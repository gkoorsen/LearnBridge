CREATE TABLE public.organisation
(
    id                   serial,
    uid                  character varying           NOT NULL,
    created_datetime     timestamp without time zone NOT NULL,
    created_by           character varying           NOT NULL,
    name                 character varying           NOT NULL,
    disabled             boolean                     NOT NULL,
    updated_datetime     timestamp without time zone,
    updated_by           character varying,
    PRIMARY KEY (id)
);

ALTER TABLE IF EXISTS public.organization
    OWNER to "postgres_admin";


CREATE TABLE public.modules
(
    id                   serial,
    uid                  character varying           NOT NULL,
    organisation_uid     character varying           NOT NULL,
    created_datetime     timestamp without time zone NOT NULL,
    created_by           character varying           NOT NULL,
    name                 character varying           NOT NULL,
    disabled             boolean                     NOT NULL,
    updated_datetime     timestamp without time zone,
    updated_by           character varying,
    PRIMARY KEY (id)
);

ALTER TABLE IF EXISTS public.modules
    OWNER to "postgres_admin";


CREATE TABLE public.users
(
    id                   serial,
    uid                  character varying           NOT NULL,
    created_datetime     timestamp without time zone NOT NULL,
    created_by           character varying           NOT NULL,
    organisation_uid     character varying           NOT NULL,
    module_uid           character varying           NOT NULL,
    username             character varying           NOT NULL,
    name                 character varying           NOT NULL,
    surname              character varying           NOT NULL,
    email_address        character varying           NOT NULL,
    role                 character varying           NOT NULL,
    password             character varying           NOT NULL,
    mfa_disabled         boolean                     NOT NULL,
    mfa_secret           character varying,
    assistants_uids      character varying           NOT NULL,
    disabled             boolean                     NOT NULL,
    PRIMARY KEY (id)
);

ALTER TABLE IF EXISTS public.users
    OWNER to "postgres_admin";


CREATE TABLE public.assistants
(
    id                      serial                      NOT NULL,
    uid                     character varying           NOT NULL,
    organisation_uid        character varying           NOT NULL,
    module_uid              character varying           NOT NULL,
    created_datetime        timestamp without time zone NOT NULL,
    created_by              character varying           NOT NULL,
    llm_type                character varying           NOT NULL,
    name                    character varying           NOT NULL,
    description             character varying           NOT NULL,
    additional_instructions character varying,
    llm_organisation_id     character varying           NOT NULL,
    llm_assistant_id        character varying           NOT NULL,
    llm_api_key             character varying           NOT NULL,
    disabled                boolean                     NOT NULL,
    updated_datetime        timestamp without time zone,
    updated_by              character varying,
    disabled_by             character varying,
    disabled_datetime       timestamp without time zone,
    PRIMARY KEY (id)
);

ALTER TABLE IF EXISTS public.assistants
    OWNER to "postgres_admin";


CREATE TABLE public.chats
(
    id                      serial                      NOT NULL,
    uid                     character varying           NOT NULL,
    assistants_uid          character varying           NOT NULL,
    created_datetime        timestamp without time zone NOT NULL,
    created_by              character varying           NOT NULL,
    description             character varying           NOT NULL,
    updated_datetime        timestamp without time zone,
    llm_assistant_id        character varying           NOT NULL,
    llm_thread_id           character varying           NOT NULL,
    llm_first_message_id    character varying           NOT NULL,
    llm_last_message_id     character varying           NOT NULL,
    llm_response_object     character varying           NOT NULL,
    PRIMARY KEY (id)
);

ALTER TABLE IF EXISTS public.chats
    OWNER to "postgres_admin";
