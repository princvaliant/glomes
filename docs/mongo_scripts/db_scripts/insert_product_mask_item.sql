INSERT INTO
`glo`.`product_mask_item`
(
    version,
    code,
    derived_product_id,
    product_mask_id
)
SELECT
    version,
    code,
    derived_product_id,
    3
FROM
    `glo`.`product_mask_item`
