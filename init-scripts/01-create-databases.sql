-- Runs once when the postgres container is first created (empty data volume).
-- Creates one database per microservice. The default POSTGRES_DB from the
-- environment creates the first one (catalog_db); this script handles the rest.

CREATE DATABASE user_db;
CREATE DATABASE circulation_db;