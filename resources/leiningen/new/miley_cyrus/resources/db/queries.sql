-- :name create-memory! :! :n
-- :doc creates a new memory record
INSERT INTO memories
(id, memory_text)
VALUES (:id, :memory-text)

-- :name get-memory :? :1
-- :doc retrieves a memory record given the id
SELECT * FROM memories
WHERE id = :id

-- :name update-memory! :! :n
-- :doc updates an existing memory record
UPDATE memories
SET memory_text = :memory-text
WHERE id = :id

-- :name delete-memory! :! :n
-- :doc deletes a memory record given the id
DELETE FROM memories
WHERE id = :id
