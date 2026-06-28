-- 1. Create schedule table
CREATE TABLE schedule (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    start_date DATE,
    end_date DATE,
    status VARCHAR(255),
    details TEXT,
    analysis_result TEXT,
    steps JSONB
);

-- 2. Create project table
CREATE TABLE project (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    budget DOUBLE PRECISION,
    status VARCHAR(255),
    analysis_result TEXT,
    resource_analysis_result TEXT,
    schedule_id BIGINT UNIQUE REFERENCES schedule(id)
);

-- 3. Create resource_allocation table
CREATE TABLE resource_allocation (
    id BIGSERIAL PRIMARY KEY,
    resource_name VARCHAR(255) NOT NULL,
    role VARCHAR(255),
    hours_per_week INTEGER,
    cost_per_hour DOUBLE PRECISION,
    quantity INTEGER,
    project_id BIGINT REFERENCES project(id)
);

-- 4. Seed schedule table (5 records)
INSERT INTO schedule (title, start_date, end_date, status, details, analysis_result, steps) VALUES
('Cronograma Projeto Alpha', '2026-07-01', '2026-12-31', 'PLANEJADO', 'Detalhamento do cronograma do Projeto Alpha', NULL, '[{"stepName": "Especificacao", "description": "Criar doc de specs", "sequence": 1, "daysRequired": 10, "completed": false}]'::jsonb),
('Cronograma Projeto Beta', '2026-08-01', '2027-02-28', 'EM_ANDAMENTO', 'Cronograma operacional do Projeto Beta', NULL, '[{"stepName": "Desenvolvimento", "description": "Fase de codificacao", "sequence": 1, "daysRequired": 30, "completed": false}]'::jsonb),
('Cronograma Projeto Gamma', '2026-09-15', '2026-11-30', 'PLANEJADO', 'Foco em analise de infraestrutura', NULL, '[{"stepName": "Provisionamento", "description": "Subir ambientes no localstack", "sequence": 1, "daysRequired": 5, "completed": false}]'::jsonb),
('Cronograma Projeto Delta', '2026-05-01', '2026-06-30', 'CONCLUIDO', 'Cronograma finalizado de analise', NULL, '[{"stepName": "Homologacao", "description": "Validacao do cliente", "sequence": 1, "daysRequired": 7, "completed": true}]'::jsonb),
('Cronograma Projeto Epsilon', '2026-10-01', '2027-04-30', 'PLANEJADO', 'Projeto de expansao internacional', NULL, '[{"stepName": "Internacionalizacao", "description": "Traducao de termos", "sequence": 1, "daysRequired": 15, "completed": false}]'::jsonb);

-- 5. Seed project table (5 records)
INSERT INTO project (name, description, budget, status, analysis_result, resource_analysis_result, schedule_id) VALUES
('Projeto Alpha', 'Desenvolvimento do novo portal corporativo da empresa', 150000.00, 'INICIADO', NULL, NULL, 1),
('Projeto Beta', 'Migracao de banco de dados legado para solucao cloud native', 280000.00, 'EM_PLANEJAMENTO', NULL, NULL, 2),
('Projeto Gamma', 'Implementacao de pipeline CI/CD unificado', 75000.00, 'AGUARDANDO_APROVACAO', NULL, NULL, 3),
('Projeto Delta', 'Analise de performance e otimizacao de APIs', 45000.00, 'FINALIZADO', NULL, NULL, 4),
('Projeto Epsilon', 'Pesquisa e desenvolvimento de modulos de seguranca', 320000.00, 'EM_PLANEJAMENTO', NULL, NULL, 5);

-- 6. Seed resource_allocation table (5 records)
INSERT INTO resource_allocation (resource_name, role, hours_per_week, cost_per_hour, quantity, project_id) VALUES
('Alice Silva', 'Desenvolvedora Backend Senior', 40, 75.50, 1, 1),
('Bruno Souza', 'Arquiteto de Solucoes', 20, 120.00, 1, 2),
('Carla Dias', 'Engenheira de DevOps', 30, 90.00, 1, 3),
('Diego Lima', 'Analista de QA', 40, 50.00, 2, 4),
('Elena Torres', 'Especialista em Seguranca', 15, 150.00, 1, 5);
