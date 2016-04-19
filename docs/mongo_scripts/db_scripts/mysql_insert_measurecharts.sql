INSERT INTO `glo`.`measure_chart`
(`version`,
`result_type`,
`revision`,
`test_type`,
`x_field`,
`y_field`,
`name`,
`filter_field`,
`group_field`,
`group_title`,
`x_title`,
`y_title`,
`ord`,
`filter_title`)
SELECT
`measure_chart`.`version`,
`measure_chart`.`result_type`,
`measure_chart`.`revision`,
'd65_before_encap',
`measure_chart`.`x_field`,
`measure_chart`.`y_field`,
`measure_chart`.`name`,
`measure_chart`.`filter_field`,
`measure_chart`.`group_field`,
`measure_chart`.`group_title`,
`measure_chart`.`x_title`,
`measure_chart`.`y_title`,
`measure_chart`.`ord`,
`measure_chart`.`filter_title`
FROM `glo`.`measure_chart` where test_type = 'd65_after_encap';

