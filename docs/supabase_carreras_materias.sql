-- =============================================================
-- Tablas de carreras y materias para MainActivity2
-- Ejecutar en Supabase: SQL Editor -> New query -> Run
-- =============================================================

-- 1. Tablas
create table if not exists public.carreras (
    id          bigint generated always as identity primary key,
    nombre      text not null unique,
    created_at  timestamptz not null default now()
);

create table if not exists public.materias (
    id          bigint generated always as identity primary key,
    carrera_id  bigint not null references public.carreras (id) on delete cascade,
    nombre      text not null,
    semestre    smallint check (semestre between 1 and 10),
    created_at  timestamptz not null default now(),
    unique (carrera_id, nombre)
);

create index if not exists materias_carrera_id_idx on public.materias (carrera_id);

-- 2. Seguridad: RLS activo con lectura pública (rol anon), igual que en alumnos
alter table public.carreras enable row level security;
alter table public.materias enable row level security;

drop policy if exists "lectura_carreras" on public.carreras;
create policy "lectura_carreras"
    on public.carreras for select
    to anon
    using (true);

drop policy if exists "lectura_materias" on public.materias;
create policy "lectura_materias"
    on public.materias for select
    to anon
    using (true);

-- 3. Datos de ejemplo (puede cambiarlos por los reales)
insert into public.carreras (nombre) values
    ('Ingeniería de Software'),
    ('Telemática'),
    ('Sistemas de Información')
on conflict (nombre) do nothing;

insert into public.materias (carrera_id, nombre, semestre)
select c.id, m.nombre, m.semestre
from (values
    ('Ingeniería de Software',  'Fundamentos de Programación', 1),
    ('Ingeniería de Software',  'Estructuras de Datos',        2),
    ('Ingeniería de Software',  'Base de Datos',               3),
    ('Ingeniería de Software',  'Aplicaciones Móviles',        4),
    ('Ingeniería de Software',  'Ingeniería de Requisitos',    5),
    ('Telemática',              'Redes de Computadoras',       3),
    ('Telemática',              'Comunicaciones Inalámbricas', 4),
    ('Telemática',              'Seguridad de Redes',          5),
    ('Sistemas de Información', 'Análisis de Sistemas',        3),
    ('Sistemas de Información', 'Inteligencia de Negocios',    5)
) as m (carrera, nombre, semestre)
join public.carreras c on c.nombre = m.carrera
on conflict (carrera_id, nombre) do nothing;

-- 4. Verificación
select c.nombre as carrera, m.semestre, m.nombre as materia
from public.materias m
join public.carreras c on c.id = m.carrera_id
order by c.nombre, m.semestre, m.nombre;
