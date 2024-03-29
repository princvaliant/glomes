INSERT INTO
`glo`.`variable`
(
    version,
    allow_blank,
    data_type,
    default_value,
    dir,
    editor,
    hidden,
    idx,
    list_values,
    name,
    process_id,
    process_step_id,
    title,
    width,
    read_only,
    eval_script,
    list_force_selection,
    include_in_search
)
SELECT
    version,
    allow_blank,
    data_type,
    default_value,
    dir,
    editor,
    hidden,
    idx,
    list_values,
    name,
    null,
    1573,
    title,
    width,
    0,
    eval_script,
    list_force_selection,
    include_in_search
FROM
    `glo`.`variable`
WHERE
process_step_id = 99 ;