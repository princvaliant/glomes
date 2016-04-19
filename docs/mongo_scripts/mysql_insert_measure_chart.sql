INSERT INTO `glo`.`measure_chart`
(
`version`,
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
	version,
	result_type,
	revision,
	"iblu_rel_test",
	x_field,
	y_field,
	name,
	filter_field,
	group_field,
	group_title,
	x_title,
	y_title,
	ord,
	filter_title
FROM
    `glo`.`measure_chart`
WHERE
id >= 181 and id <= 190 ;
