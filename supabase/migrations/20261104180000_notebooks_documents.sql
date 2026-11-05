-- Notebooks and their PDF sources (feature: notebooks-documents).
-- Every rule is enforced here so a direct API call has the same limits as the app.

-- ---------------------------------------------------------------------------
-- Tables
-- ---------------------------------------------------------------------------

create table public.notebooks (
    id uuid primary key default gen_random_uuid(),
    user_id uuid not null default auth.uid() references auth.users (id) on delete cascade,
    title text not null check (char_length(title) between 1 and 200),
    created_at timestamptz not null default now()
);

create index notebooks_user_id_created_at_idx on public.notebooks (user_id, created_at desc);

create table public.documents (
    id uuid primary key default gen_random_uuid(),
    notebook_id uuid not null references public.notebooks (id) on delete cascade,
    user_id uuid not null default auth.uid() references auth.users (id) on delete cascade,
    file_name text not null check (char_length(file_name) between 1 and 255),
    size_bytes bigint not null check (size_bytes > 0 and size_bytes <= 20971520),
    storage_path text not null unique,
    created_at timestamptz not null default now()
);

create index documents_notebook_id_idx on public.documents (notebook_id);
create index documents_user_id_idx on public.documents (user_id);

-- ---------------------------------------------------------------------------
-- Access: signed-in users read and create only their own rows. No update or delete yet.
-- ---------------------------------------------------------------------------

alter table public.notebooks enable row level security;
alter table public.documents enable row level security;

revoke all on public.notebooks, public.documents from anon, authenticated;
grant select, insert on public.notebooks, public.documents to authenticated;

create policy notebooks_select_own on public.notebooks
    for select to authenticated
    using ((select auth.uid()) = user_id);

create policy notebooks_insert_own on public.notebooks
    for insert to authenticated
    with check ((select auth.uid()) = user_id);

create policy documents_select_own on public.documents
    for select to authenticated
    using ((select auth.uid()) = user_id);

create policy documents_insert_own on public.documents
    for insert to authenticated
    with check (
        (select auth.uid()) = user_id
        and exists (
            select 1 from public.notebooks n
            where n.id = notebook_id and n.user_id = (select auth.uid())
        )
    );

-- ---------------------------------------------------------------------------
-- At most 20 sources per notebook. The advisory lock serialises concurrent
-- inserts into the same notebook so two uploads cannot both pass the count.
-- ---------------------------------------------------------------------------

create function public.enforce_document_limit()
returns trigger
language plpgsql
security invoker
set search_path = ''
as $$
declare
    v_count integer;
begin
    perform pg_advisory_xact_lock(hashtextextended(new.notebook_id::text, 0));
    select count(*) into v_count from public.documents where notebook_id = new.notebook_id;
    if v_count >= 20 then
        raise exception 'source_limit_reached' using errcode = 'P0001';
    end if;
    return new;
end;
$$;

revoke execute on function public.enforce_document_limit() from public, anon, authenticated;

create trigger documents_enforce_limit
    before insert on public.documents
    for each row execute function public.enforce_document_limit();

-- ---------------------------------------------------------------------------
-- Storage: private bucket, PDF only, 20 MB. Files live under {user_id}/...
-- ---------------------------------------------------------------------------

insert into storage.buckets (id, name, public, file_size_limit, allowed_mime_types)
values ('documents', 'documents', false, 20971520, array['application/pdf']);

create policy documents_objects_insert_own on storage.objects
    for insert to authenticated
    with check (bucket_id = 'documents' and (storage.foldername(name))[1] = (select auth.uid())::text);

create policy documents_objects_select_own on storage.objects
    for select to authenticated
    using (bucket_id = 'documents' and (storage.foldername(name))[1] = (select auth.uid())::text);

-- Only used to remove a file whose upload did not finish; the app offers no delete.
create policy documents_objects_delete_own on storage.objects
    for delete to authenticated
    using (bucket_id = 'documents' and (storage.foldername(name))[1] = (select auth.uid())::text);

-- ---------------------------------------------------------------------------
-- Rows are created in one transaction, only after the file is in Storage.
-- The path is computed here (not trusted from the app) and the size comes
-- from the stored object.
-- ---------------------------------------------------------------------------

create schema if not exists private;
grant usage on schema private to authenticated;

create function private.uploaded_pdf_size(p_notebook_id uuid, p_document_id uuid)
returns bigint
language plpgsql
security invoker
set search_path = ''
as $$
declare
    v_size bigint;
begin
    if auth.uid() is null then
        raise exception 'not_authenticated' using errcode = '28000';
    end if;
    select (o.metadata ->> 'size')::bigint into v_size
    from storage.objects o
    where o.bucket_id = 'documents'
      and o.name = private.document_path(p_notebook_id, p_document_id);
    if v_size is null then
        raise exception 'file_not_found' using errcode = 'P0002';
    end if;
    return v_size;
end;
$$;

create function private.document_path(p_notebook_id uuid, p_document_id uuid)
returns text
language sql
stable
security invoker
set search_path = ''
as $$
    select auth.uid()::text || '/' || p_notebook_id::text || '/' || p_document_id::text || '.pdf';
$$;

revoke execute on all functions in schema private from public, anon;
grant execute on all functions in schema private to authenticated;

create function public.create_notebook_with_document(
    p_notebook_id uuid,
    p_title text,
    p_document_id uuid,
    p_file_name text
)
returns public.notebooks
language plpgsql
security invoker
set search_path = ''
as $$
declare
    v_size bigint := private.uploaded_pdf_size(p_notebook_id, p_document_id);
    v_notebook public.notebooks;
begin
    insert into public.notebooks (id, user_id, title)
    values (p_notebook_id, auth.uid(), p_title)
    returning * into v_notebook;

    insert into public.documents (id, notebook_id, user_id, file_name, size_bytes, storage_path)
    values (p_document_id, p_notebook_id, auth.uid(), p_file_name, v_size,
            private.document_path(p_notebook_id, p_document_id));

    return v_notebook;
end;
$$;

create function public.add_document(
    p_notebook_id uuid,
    p_document_id uuid,
    p_file_name text
)
returns public.documents
language plpgsql
security invoker
set search_path = ''
as $$
declare
    v_size bigint := private.uploaded_pdf_size(p_notebook_id, p_document_id);
    v_document public.documents;
begin
    insert into public.documents (id, notebook_id, user_id, file_name, size_bytes, storage_path)
    values (p_document_id, p_notebook_id, auth.uid(), p_file_name, v_size,
            private.document_path(p_notebook_id, p_document_id))
    returning * into v_document;

    return v_document;
end;
$$;

revoke execute on function public.create_notebook_with_document(uuid, text, uuid, text) from public, anon;
revoke execute on function public.add_document(uuid, uuid, text) from public, anon;
grant execute on function public.create_notebook_with_document(uuid, text, uuid, text) to authenticated;
grant execute on function public.add_document(uuid, uuid, text) to authenticated;
