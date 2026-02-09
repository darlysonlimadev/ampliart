create schema if not exists ampliart;

alter table if exists public.movimentacao_estoque set schema ampliart;
alter table if exists public.orcamento_item set schema ampliart;
alter table if exists public.orcamento set schema ampliart;
alter table if exists public.produto set schema ampliart;

create table if not exists ampliart.categoria (
    id bigserial primary key,
    nome varchar(120) not null
);

insert into ampliart.categoria (nome)
select 'Sem categoria'
where not exists (select 1 from ampliart.categoria where nome = 'Sem categoria');

alter table if exists ampliart.produto add column if not exists categoria_id bigint;

update ampliart.produto
set categoria_id = (select id from ampliart.categoria where nome = 'Sem categoria')
where categoria_id is null;

alter table ampliart.produto alter column categoria_id set not null;

do $$
begin
    if not exists (
        select 1
        from pg_constraint
        where conname = 'fk_produto_categoria'
    ) then
        alter table ampliart.produto
            add constraint fk_produto_categoria
            foreign key (categoria_id) references ampliart.categoria (id);
    end if;
end $$;

create index if not exists idx_produto_categoria on ampliart.produto (categoria_id);
