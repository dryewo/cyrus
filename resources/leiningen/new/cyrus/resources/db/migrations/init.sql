SET search_path TO public;
CREATE EXTENSION IF NOT EXISTS hstore;
CREATE SCHEMA IF NOT EXISTS {{prefix}}_data;
SET search_path TO {{prefix}}_data;
