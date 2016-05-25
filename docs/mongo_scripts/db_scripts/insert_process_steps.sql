INSERT INTO
`glo`.`process_step`
(
    version,
    operation_id,
    process_id,
    task_key,
    allow_split,
    idx,
    allow_sync,
    filter_by_company,
    allow_import,
    allow_rework
)
SELECT
    version,
    operation_id,
    38,
    task_key,
    allow_split,
    idx,
    allow_sync,
    filter_by_company,
    allow_import,
    allow_rework
FROM
    `glo`.`process_step`
WHERE
process_id = 37