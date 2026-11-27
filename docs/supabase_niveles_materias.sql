-- =============================================================
-- Migración a la guía "Controles de Selección":
-- las materias se filtran por NIVEL (1 a 10).
-- Requiere haber ejecutado antes docs/supabase_carreras_materias.sql
-- Ejecutar en Supabase: SQL Editor -> New query -> Run
-- =============================================================

-- 1. Renombrar la columna semestre -> nivel (solo si aún no se hizo)
do $$
begin
    if exists (
        select 1 from information_schema.columns
        where table_schema = 'public' and table_name = 'materias' and column_name = 'semestre'
    ) then
        alter table public.materias rename column semestre to nivel;
    end if;
end $$;

-- Si la tabla no tenía la columna, se crea
alter table public.materias add column if not exists nivel smallint;

-- Rango válido de niveles (1 a 10), igual que el string-array "niveles" de la app
alter table public.materias drop constraint if exists materias_semestre_check;
alter table public.materias drop constraint if exists materias_nivel_check;
alter table public.materias add constraint materias_nivel_check check (nivel between 1 and 10);

-- La app ya no filtra por carrera: se permite materia sin carrera
alter table public.materias alter column carrera_id drop not null;

create index if not exists materias_nivel_idx on public.materias (nivel);

-- 2. Materias de Ingeniería de Software para los 10 niveles
--    (si la materia ya existe, solo se actualiza su nivel)
insert into public.materias (carrera_id, nombre, nivel)
select c.id, m.nombre, m.nivel
from (values
    ('Fundamentos de Programación',           1),
    ('Matemáticas Discretas',                 1),
    ('Introducción a la Ingeniería de Software', 1),
    ('Programación Orientada a Objetos',      2),
    ('Estructuras de Datos',                  2),
    ('Cálculo',                               2),
    ('Base de Datos',                         3),
    ('Arquitectura de Computadoras',          3),
    ('Ingeniería de Requisitos',              4),
    ('Sistemas Operativos',                   4),
    ('Diseño de Software',                    5),
    ('Redes de Computadoras',                 5),
    ('Aplicaciones Móviles',                  6),
    ('Aplicaciones Web',                      6),
    ('Pruebas de Software',                   7),
    ('Gestión de Proyectos de Software',      7),
    ('Seguridad Informática',                 8),
    ('Inteligencia Artificial',               8),
    ('Calidad de Software',                   9),
    ('Emprendimiento',                        9),
    ('Prácticas Preprofesionales',           10),
    ('Trabajo de Titulación',                10)
) as m (nombre, nivel)
join public.carreras c on c.nombre = 'Ingeniería de Software'
on conflict (carrera_id, nombre) do update set nivel = excluded.nivel;

-- 3. (Opcional) Las materias de otras carreras también aparecen al filtrar por nivel.
--    Si solo quiere ver las de Ingeniería de Software, quite el comentario y ejecute:
-- delete from public.materias
-- where carrera_id <> (select id from public.carreras where nombre = 'Ingeniería de Software');

-- 4. Verificación: materias por nivel
select nivel, nombre
from public.materias
order by nivel, nombre;
