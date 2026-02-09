create table produto (
    id bigserial primary key,
    nome varchar(255) not null,
    descricao text,
    codigo varchar(80) not null,
    preco_compra numeric(19,2) not null,
    preco_venda numeric(19,2) not null,
    quantidade_estoque integer not null,
    ativo boolean not null,
    data_cadastro timestamp not null,
    data_atualizacao timestamp not null
);

alter table produto add constraint uk_produto_codigo unique (codigo);
create index idx_produto_nome on produto (lower(nome));

create table orcamento (
    id bigserial primary key,
    cliente_nome varchar(255) not null,
    cliente_telefone varchar(50) not null,
    cliente_email varchar(255),
    status varchar(30) not null,
    total_bruto numeric(19,2) not null,
    percentual_ajuste numeric(5,2),
    tipo_ajuste varchar(15),
    valor_ajuste numeric(19,2),
    total_final numeric(19,2) not null,
    data_cadastro timestamp not null,
    data_atualizacao timestamp not null,
    data_conclusao timestamp
);

create index idx_orcamento_status on orcamento (status);
create index idx_orcamento_conclusao on orcamento (data_conclusao);

create table orcamento_item (
    id bigserial primary key,
    orcamento_id bigint not null,
    produto_id bigint not null,
    quantidade integer not null,
    preco_unitario numeric(19,2) not null,
    subtotal numeric(19,2) not null,
    constraint fk_orcamento_item_orcamento foreign key (orcamento_id) references orcamento (id),
    constraint fk_orcamento_item_produto foreign key (produto_id) references produto (id)
);

create index idx_orcamento_item_orcamento on orcamento_item (orcamento_id);
create index idx_orcamento_item_produto on orcamento_item (produto_id);

create table movimentacao_estoque (
    id bigserial primary key,
    produto_id bigint not null,
    tipo varchar(10) not null,
    quantidade integer not null,
    motivo varchar(255) not null,
    data_movimentacao timestamp not null,
    constraint fk_movimentacao_produto foreign key (produto_id) references produto (id)
);

create index idx_movimentacao_produto on movimentacao_estoque (produto_id);
