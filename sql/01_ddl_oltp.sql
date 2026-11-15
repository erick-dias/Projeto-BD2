
    create table categoria (
       id  bigserial not null,
        nome varchar(255),
        departamento_id int8,
        primary key (id)
    );

    create table cliente (
       data_cadastro date,
        data_nascimento date,
        id int8 not null,
        primary key (id)
    );

    create table departamento (
       id  bigserial not null,
        nome varchar(255),
        primary key (id)
    );

    create table funcionario (
       cargo varchar(255),
        salario numeric(19, 2),
        id int8 not null,
        loja_id int8,
        primary key (id)
    );

    create table item_venda (
       id  bigserial not null,
        desconto numeric(19, 2),
        preco_unitario numeric(19, 2),
        quantidade int4,
        medicamento_id int8 not null,
        venda_id int8 not null,
        primary key (id)
    );

    create table laboratorio (
       id  bigserial not null,
        nome varchar(255),
        primary key (id)
    );

    create table loja (
       id  bigserial not null,
        bairro varchar(255),
        cep varchar(255),
        cidade varchar(255),
        estado varchar(255),
        logradouro varchar(255),
        numero varchar(255),
        nome varchar(255),
        primary key (id)
    );

    create table medicamento (
       id  bigserial not null,
        nome varchar(255),
        preco numeric(19, 2),
        tarja varchar(255),
        categoria_id int8,
        laboratorio_id int8,
        primary key (id)
    );

    create table medicamento_principio_ativo (
       medicamento_id int8 not null,
        principio_ativo_id int8 not null
    );

    create table pessoa (
       id  bigserial not null,
        cpf varchar(255),
        email varchar(255),
        bairro varchar(255),
        cep varchar(255),
        cidade varchar(255),
        estado varchar(255),
        logradouro varchar(255),
        numero varchar(255),
        nome varchar(255),
        primary key (id)
    );

    create table principio_ativo (
       id  bigserial not null,
        nome varchar(255),
        primary key (id)
    );

    create table receita (
       id  bigserial not null,
        crm_medico varchar(255),
        data_emissao date,
        nome_medico varchar(255),
        venda_id int8 not null,
        primary key (id)
    );

    create table venda (
       id  bigserial not null,
        data_venda date,
        forma_pagamento varchar(255),
        valor_total numeric(19, 2),
        cliente_id int8,
        funcionario_id int8,
        loja_id int8,
        primary key (id)
    );

    alter table receita 
       add constraint UK_tfj6nsp3ryv17w0xmcbperp58 unique (venda_id);

    alter table categoria 
       add constraint FK4p96j3lmrc5jf8fe8o1dtma9q 
       foreign key (departamento_id) 
       references departamento;

    alter table cliente 
       add constraint FKa710hls1tg15o3d4ok5biathy 
       foreign key (id) 
       references pessoa;

    alter table funcionario 
       add constraint FKd9ngjtrh226aoprebowwb9nxi 
       foreign key (loja_id) 
       references loja;

    alter table funcionario 
       add constraint FKpjvvctvfjm69dvye3aea5k56f 
       foreign key (id) 
       references pessoa;

    alter table item_venda 
       add constraint FKnpxefo67tovwfnwax7kubxou5 
       foreign key (medicamento_id) 
       references medicamento;

    alter table item_venda 
       add constraint FKkiky88fkai72328rhw3r3yebx 
       foreign key (venda_id) 
       references venda;

    alter table medicamento 
       add constraint FKcdvfhdehpoq6vpuib9u8e0rxy 
       foreign key (categoria_id) 
       references categoria;

    alter table medicamento 
       add constraint FKqfq5cmmiouye0nj2jmp9voao1 
       foreign key (laboratorio_id) 
       references laboratorio;

    alter table medicamento_principio_ativo 
       add constraint FKkg3en0obb6f2pi01202am861n 
       foreign key (principio_ativo_id) 
       references principio_ativo;

    alter table medicamento_principio_ativo 
       add constraint FKb302la15a1qkonlie4nrisa64 
       foreign key (medicamento_id) 
       references medicamento;

    alter table receita 
       add constraint FK3s9i8oxpwmyduhk35h1aoved5 
       foreign key (venda_id) 
       references venda;

    alter table venda 
       add constraint FK50murhuotq9h2dnxej317jjiy 
       foreign key (cliente_id) 
       references cliente;

    alter table venda 
       add constraint FKon6h4s8x1kdjwkcipq65i5my7 
       foreign key (funcionario_id) 
       references funcionario;

    alter table venda 
       add constraint FK6lg5cbp146t512l70buxjggto 
       foreign key (loja_id) 
       references loja;
