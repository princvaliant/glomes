INSERT INTO
`glo`.`product_mask_item`
(
    version,
    code,
    derived_product_id,
    product_mask_id,
    plx,
    ply,
    sizex,
    sizey,
    is_active
)
SELECT
    version,
    code,
    derived_product_id,
    product_mask_id,
    plx,
    ply,
    sizex,
    sizey,
    is_active
FROM
    `glo`.`variable`
WHERE
 product_mask_id = 14