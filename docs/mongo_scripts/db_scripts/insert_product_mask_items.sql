INSERT INTO `glo`.`product_mask_item`
(
`version`,
`code`,
`derived_product_id`,
`product_mask_id`,
`plx`,
`ply`,
`sizex`,
`sizey`,
`is_active`)
SELECT
    version,
    code,
    derived_product_id,
    17,
    plx,
    ply,
    sizex,
    sizey,
    is_active
FROM
    product_mask_item
WHERE
    product_mask_id = 16

