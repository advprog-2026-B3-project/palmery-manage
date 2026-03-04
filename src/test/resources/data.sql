INSERT INTO mandor (id, nama, kebun_id, kontak)
VALUES ('MDR-1', 'Slamet Riyadi', 'KEB-1', '08123456789');

INSERT INTO supir (id, nama, kebun_id, kontak)
VALUES ('DRV-1', 'Eko Prasetyo', 'KEB-1', '0811111111'),
       ('DRV-2', 'Fajar Rahman', 'KEB-1', '0822222222');

INSERT INTO panen (id, mandor_id, kebun_id, berat_kg, ready_for_delivery, status)
VALUES ('PAN-1', 'MDR-1', 'KEB-1', 145, true, 'SIAP_ANGKUT'),
       ('PAN-2', 'MDR-1', 'KEB-1', 132, true, 'SIAP_ANGKUT'),
       ('PAN-3', 'MDR-1', 'KEB-1', 120, false, 'APPROVED');

