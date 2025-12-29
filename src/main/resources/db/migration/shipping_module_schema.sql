-- =====================================================
-- FB SHIPPING FLOW DASHBOARD - DATABASE SCHEMA
-- =====================================================
-- This schema supports hierarchical load/unload tracking:
-- Mother Vessel → Multiple Lighters → Multiple Trucks → Product Details
-- =====================================================

-- Create shipment_cycles table (Main table for mother vessel arrivals)
CREATE TABLE IF NOT EXISTS shipment_cycles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    consignee VARCHAR(255) NOT NULL COMMENT 'e.g., PDL',
    mother_vessel_name VARCHAR(255) NOT NULL COMMENT 'e.g., MEGHNA ENERGY',
    arrival_date DATE NOT NULL,
    total_incoming_quantity DOUBLE NOT NULL,
    item_type VARCHAR(255) NOT NULL COMMENT 'e.g., 10-20 Stone',
    status ENUM('PENDING', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED') NOT NULL DEFAULT 'PENDING',
    shipment_document_path VARCHAR(500),
    flow_summary VARCHAR(1000) COMMENT 'Auto-generated flow description',
    institute_id BIGINT NOT NULL,
    created_by_user_id BIGINT,
    assigned_to_user_id BIGINT COMMENT 'Officer assigned to this shipment',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (institute_id) REFERENCES institutes(id) ON DELETE CASCADE,
    FOREIGN KEY (created_by_user_id) REFERENCES users(id) ON DELETE SET NULL,
    FOREIGN KEY (assigned_to_user_id) REFERENCES users(id) ON DELETE SET NULL,
    INDEX idx_institute_id (institute_id),
    INDEX idx_status (status),
    INDEX idx_arrival_date (arrival_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Main shipment cycles from mother vessels';

-- Create lighter_loadings table (Unload from mother vessel to lighters)
CREATE TABLE IF NOT EXISTS lighter_loadings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    lighter_name VARCHAR(255) NOT NULL COMMENT 'e.g., MV A&J Traders 04',
    destination VARCHAR(255),
    unloading_point VARCHAR(255) COMMENT 'e.g., Tulatoli',
    loading_date DATE NOT NULL,
    loaded_quantity DOUBLE NOT NULL,
    lighter_cost DOUBLE DEFAULT 0.0,
    status ENUM('PENDING', 'LOADED', 'UNLOADED', 'IN_TRANSIT', 'DELIVERED') NOT NULL DEFAULT 'PENDING',
    lighter_document_path VARCHAR(500),
    shipment_cycle_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (shipment_cycle_id) REFERENCES shipment_cycles(id) ON DELETE CASCADE,
    INDEX idx_shipment_cycle_id (shipment_cycle_id),
    INDEX idx_loading_date (loading_date),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Loading onto lighters from mother vessel (1 to many)';

-- Create truck_unloadings table (Unload from lighters to trucks)
CREATE TABLE IF NOT EXISTS truck_unloadings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    challan VARCHAR(255) COMMENT 'Challan number',
    conveyance_name VARCHAR(255) NOT NULL COMMENT 'e.g., MV Innex 05',
    number_of_trucks INT DEFAULT 1 COMMENT 'Number of trucks',
    discharging_location VARCHAR(255) COMMENT 'e.g., Tulatoli',
    destination VARCHAR(255) COMMENT 'e.g., Motirhat',
    party VARCHAR(255) COMMENT 'e.g., PDL',
    unloading_date DATE NOT NULL,
    unloaded_quantity DOUBLE NOT NULL,
    unloading_cost DOUBLE DEFAULT 0.0,
    status ENUM('PENDING', 'LOADED', 'UNLOADED', 'IN_TRANSIT', 'DELIVERED') NOT NULL DEFAULT 'PENDING',
    depends_on_lighter_completion BOOLEAN DEFAULT TRUE COMMENT 'Dependency flag',
    lighter_loading_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (lighter_loading_id) REFERENCES lighter_loadings(id) ON DELETE CASCADE,
    INDEX idx_lighter_loading_id (lighter_loading_id),
    INDEX idx_unloading_date (unloading_date),
    INDEX idx_destination (destination),
    INDEX idx_party (party)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Unloading from lighters to trucks (many to many)';

-- Create product_details table (Granular item-level costs)
CREATE TABLE IF NOT EXISTS product_details (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    item VARCHAR(255) NOT NULL COMMENT 'e.g., 10-20 Stone',
    delivery_quantity DOUBLE COMMENT 'e.g., 480, 350',
    survey_quantity DOUBLE COMMENT 'e.g., 100, 115',
    lighter_cost DOUBLE DEFAULT 0.0,
    unloading_cost DOUBLE DEFAULT 0.0,
    truck_transport_cost DOUBLE DEFAULT 0.0,
    truck_unloading_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (truck_unloading_id) REFERENCES truck_unloadings(id) ON DELETE CASCADE,
    INDEX idx_truck_unloading_id (truck_unloading_id),
    INDEX idx_item (item)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Product details with cost breakdown';

-- =====================================================
-- DEMO DATA based on your requirements
-- =====================================================

-- Sample Shipment Cycle (Mother Vessel: MEGHNA ENERGY for PDL)
INSERT INTO shipment_cycles (
    id, consignee, mother_vessel_name, arrival_date, total_incoming_quantity, 
    item_type, status, flow_summary, institute_id, created_by_user_id
) VALUES (
    1, 'PDL', 'MEGHNA ENERGY', '2025-11-10', 5000.0, '10-20 Stone', 
    'IN_PROGRESS', 'Unloaded from 1 Mother Vessel (MEGHNA ENERGY) to 3 Lighter(s), then to 8 Truck(s)', 
    1, 1
);

-- Lighter Loading 1 (MV A&J Traders 04)
INSERT INTO lighter_loadings (
    id, lighter_name, destination, unloading_point, loading_date, 
    loaded_quantity, lighter_cost, status, shipment_cycle_id
) VALUES (
    1, 'MV A&J Traders 04', 'O/A - Tulatoli', 'Tulatoli', '2025-10-29', 
    1500.0, 1500.0, 'LOADED', 1
);

-- Lighter Loading 2 (MV Innex 05)
INSERT INTO lighter_loadings (
    id, lighter_name, destination, unloading_point, loading_date, 
    loaded_quantity, lighter_cost, status, shipment_cycle_id
) VALUES (
    2, 'MV Innex 05', 'O/A - Tulatoli', 'Tulatoli', '2025-10-30', 
    2000.0, 2000.0, 'LOADED', 1
);

-- Lighter Loading 3 (MV Sahara 11)
INSERT INTO lighter_loadings (
    id, lighter_name, destination, unloading_point, loading_date, 
    loaded_quantity, lighter_cost, status, shipment_cycle_id
) VALUES (
    3, 'MV Sahara 11', 'O/A - Kaderpandithat', 'Kaderpandithat', '2025-10-31', 
    1500.0, 1500.0, 'IN_TRANSIT', 1
);

-- Truck Unloadings from Lighter 1 (MV A&J Traders 04 → 3 Trucks)
INSERT INTO truck_unloadings (
    id, challan, conveyance_name, number_of_trucks, discharging_location, 
    destination, party, unloading_date, unloaded_quantity, unloading_cost, 
    status, lighter_loading_id
) VALUES 
(1, 'CH-001', 'MV A&J Traders 04', 1, 'Tulatoli', 'Motirhat', 'PDL', 
 '2025-11-01', 480.0, 100.0, 'UNLOADED', 1),
(2, 'CH-002', 'MV A&J Traders 04', 1, 'Tulatoli', 'Kaderpandithat', 'PDL', 
 '2025-11-01', 500.0, 120.0, 'UNLOADED', 1),
(3, 'CH-003', 'MV A&J Traders 04', 1, 'Tulatoli', 'Motirhat', 'PDL', 
 '2025-11-02', 520.0, 110.0, 'IN_TRANSIT', 1);

-- Truck Unloadings from Lighter 2 (MV Innex 05 → 3 Trucks)
INSERT INTO truck_unloadings (
    id, challan, conveyance_name, number_of_trucks, discharging_location, 
    destination, party, unloading_date, unloaded_quantity, unloading_cost, 
    status, lighter_loading_id
) VALUES 
(4, 'CH-004', 'MV Innex 05', 2, 'Tulatoli', 'Motirhat', 'PDL', 
 '2025-11-02', 700.0, 150.0, 'DELIVERED', 2),
(5, 'CH-005', 'MV Innex 05', 1, 'Tulatoli', 'Kaderpandithat', 'PDL', 
 '2025-11-03', 650.0, 140.0, 'DELIVERED', 2),
(6, 'CH-006', 'MV Innex 05', 1, 'Tulatoli', 'Motirhat', 'PDL', 
 '2025-11-03', 650.0, 135.0, 'IN_TRANSIT', 2);

-- Truck Unloadings from Lighter 3 (MV Sahara 11 → 2 Trucks)
INSERT INTO truck_unloadings (
    id, challan, conveyance_name, number_of_trucks, discharging_location, 
    destination, party, unloading_date, unloaded_quantity, unloading_cost, 
    status, lighter_loading_id
) VALUES 
(7, 'CH-007', 'MV Sahara 11', 1, 'Kaderpandithat', 'Motirhat', 'PDL', 
 '2025-11-04', 750.0, 160.0, 'PENDING', 3),
(8, 'CH-008', 'MV Sahara 11', 1, 'Kaderpandithat', 'Kaderpandithat', 'PDL', 
 '2025-11-05', 750.0, 155.0, 'PENDING', 3);

-- Product Details for Truck 1 (matching your screenshot: 480, 100, 550 → 1130)
INSERT INTO product_details (
    item, delivery_quantity, survey_quantity, lighter_cost, 
    unloading_cost, truck_transport_cost, truck_unloading_id
) VALUES 
('10-20 Stone', 480.0, 100.0, 480.0, 100.0, 550.0, 1);

-- Product Details for Truck 2
INSERT INTO product_details (
    item, delivery_quantity, survey_quantity, lighter_cost, 
    unloading_cost, truck_transport_cost, truck_unloading_id
) VALUES 
('10-20 Stone', 500.0, 115.0, 500.0, 120.0, 600.0, 2);

-- Product Details for Truck 3
INSERT INTO product_details (
    item, delivery_quantity, survey_quantity, lighter_cost, 
    unloading_cost, truck_transport_cost, truck_unloading_id
) VALUES 
('10-20 Stone', 520.0, 110.0, 520.0, 110.0, 620.0, 3);

-- Product Details for Truck 4
INSERT INTO product_details (
    item, delivery_quantity, survey_quantity, lighter_cost, 
    unloading_cost, truck_transport_cost, truck_unloading_id
) VALUES 
('10-20 Stone', 700.0, 150.0, 700.0, 150.0, 800.0, 4);

-- Product Details for Truck 5 (matching: 350, 115, 250 → 830 - adjusted for demo)
INSERT INTO product_details (
    item, delivery_quantity, survey_quantity, lighter_cost, 
    unloading_cost, truck_transport_cost, truck_unloading_id
) VALUES 
('10-20 Stone', 350.0, 115.0, 350.0, 115.0, 365.0, 5);

-- Product Details for remaining trucks
INSERT INTO product_details (
    item, delivery_quantity, survey_quantity, lighter_cost, 
    unloading_cost, truck_transport_cost, truck_unloading_id
) VALUES 
('10-20 Stone', 650.0, 135.0, 650.0, 135.0, 750.0, 6),
('10-20 Stone', 750.0, 160.0, 750.0, 160.0, 850.0, 7),
('10-20 Stone', 750.0, 155.0, 750.0, 155.0, 840.0, 8);

-- =====================================================
-- VIEWS FOR REPORTING
-- =====================================================

-- View: Complete shipment flow with all relationships
CREATE OR REPLACE VIEW v_shipment_flow AS
SELECT 
    sc.id AS shipment_id,
    sc.consignee,
    sc.mother_vessel_name,
    sc.arrival_date,
    sc.total_incoming_quantity,
    sc.item_type,
    sc.status AS shipment_status,
    ll.id AS lighter_id,
    ll.lighter_name,
    ll.loaded_quantity AS lighter_loaded_quantity,
    ll.lighter_cost,
    tu.id AS truck_id,
    tu.conveyance_name AS truck_conveyance,
    tu.destination AS truck_destination,
    tu.unloaded_quantity AS truck_unloaded_quantity,
    tu.unloading_cost,
    pd.id AS product_id,
    pd.item AS product_name,
    pd.delivery_quantity,
    pd.lighter_cost + pd.unloading_cost + pd.truck_transport_cost AS product_total_cost
FROM shipment_cycles sc
LEFT JOIN lighter_loadings ll ON sc.id = ll.shipment_cycle_id
LEFT JOIN truck_unloadings tu ON ll.id = tu.lighter_loading_id
LEFT JOIN product_details pd ON tu.id = pd.truck_unloading_id;

-- View: Quantity validation report
CREATE OR REPLACE VIEW v_quantity_validation AS
SELECT 
    sc.id AS shipment_id,
    sc.mother_vessel_name,
    sc.total_incoming_quantity,
    SUM(ll.loaded_quantity) AS total_loaded,
    SUM(tu.unloaded_quantity) AS total_unloaded,
    CASE 
        WHEN SUM(ll.loaded_quantity) <= sc.total_incoming_quantity THEN 'BALANCED'
        ELSE 'OVERLOADED'
    END AS load_status
FROM shipment_cycles sc
LEFT JOIN lighter_loadings ll ON sc.id = ll.shipment_cycle_id
LEFT JOIN truck_unloadings tu ON ll.id = tu.lighter_loading_id
GROUP BY sc.id, sc.mother_vessel_name, sc.total_incoming_quantity;

-- View: Cost breakdown by stage
CREATE OR REPLACE VIEW v_cost_breakdown AS
SELECT 
    sc.id AS shipment_id,
    sc.mother_vessel_name,
    SUM(ll.lighter_cost) AS total_lighter_cost,
    SUM(tu.unloading_cost) AS total_unloading_cost,
    SUM(pd.truck_transport_cost) AS total_truck_cost,
    SUM(ll.lighter_cost + tu.unloading_cost + pd.truck_transport_cost) AS grand_total
FROM shipment_cycles sc
LEFT JOIN lighter_loadings ll ON sc.id = ll.shipment_cycle_id
LEFT JOIN truck_unloadings tu ON ll.id = tu.lighter_loading_id
LEFT JOIN product_details pd ON tu.id = pd.truck_unloading_id
GROUP BY sc.id, sc.mother_vessel_name;

-- =====================================================
-- STORED PROCEDURES for validations
-- =====================================================

DELIMITER //

-- Procedure to validate lighter unload quantities
CREATE PROCEDURE validate_lighter_quantities(IN p_lighter_id BIGINT)
BEGIN
    SELECT 
        ll.lighter_name,
        ll.loaded_quantity,
        COALESCE(SUM(tu.unloaded_quantity), 0) AS total_unloaded,
        ll.loaded_quantity - COALESCE(SUM(tu.unloaded_quantity), 0) AS remaining_capacity,
        CASE 
            WHEN COALESCE(SUM(tu.unloaded_quantity), 0) <= ll.loaded_quantity THEN 'VALID'
            ELSE 'OVERLOADED'
        END AS validation_status
    FROM lighter_loadings ll
    LEFT JOIN truck_unloadings tu ON ll.id = tu.lighter_loading_id
    WHERE ll.id = p_lighter_id
    GROUP BY ll.id, ll.lighter_name, ll.loaded_quantity;
END //

DELIMITER ;

-- =====================================================
-- INDEXES FOR PERFORMANCE
-- =====================================================

CREATE INDEX idx_shipment_consignee ON shipment_cycles(consignee);
CREATE INDEX idx_shipment_vessel ON shipment_cycles(mother_vessel_name);
CREATE INDEX idx_lighter_name ON lighter_loadings(lighter_name);
CREATE INDEX idx_truck_destination ON truck_unloadings(destination);
CREATE INDEX idx_product_item ON product_details(item);

-- =====================================================
-- END OF MIGRATION SCRIPT
-- =====================================================
