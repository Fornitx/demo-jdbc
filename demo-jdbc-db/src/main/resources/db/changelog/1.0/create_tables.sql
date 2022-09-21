--liquibase formatted sql

--changeset fornit:create_project_table
create table if not exists project
(
    id         uuid primary key     default uuid_generate_v4(),
    project_id uuid not null,
    rules      jsonb       not null,
    version    bigint      not null default 1,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now(),
    created_by varchar(50) not null,
    updated_by varchar(50) not null
);
--rollback drop table if exists project;

--changeset fornit:create_project_uniq_idx
create unique index if not exists project_uniq_idx on project (project_id);
--rollback drop index if exists project_uniq_idx;
