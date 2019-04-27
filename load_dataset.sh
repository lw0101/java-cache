#!/usr/bin/env bash
# Load data from a wkt data set of single lines into a json format
# Dataset taken from http://linkedgeodata.org/Datasets?v=sln
cat /cygdrive/c/Users/landc/Desktop/2015-11-02-Office.way.sorted.nt  \
| sed 's/"/\\&/g' > /tmp/temp \
&& awk 'NR==1, NR==1000 {printf "{\042className\042:\042%s\042,\042gwkt\042:\042%s\042},\n" , NR, $0 }' /tmp/temp > /tmp/tmp1 \
&& truncate -s-2 /tmp/tmp1 \
&& echo "[" > /tmp/tmp2 \
&& cat /tmp/tmp1 >> /tmp/tmp2 \
&& echo "]" >> /tmp/tmp2 \
&& mv /tmp/tmp2 /cygdrive/c/Users/landc/Desktop/rs.json

#Load using SQL
#$ docker cp src/main/resources/polygons.csv postgres_container:/tmp
#
#CREATE TEMP TABLE tmp_x AS SELECT class_name, gwkt FROM engine_object LIMIT 0;
#COPY tmp_x(class_name, gwkt) FROM '/tmp/polygons.csv'with (FORMAT CSV);
#INSERT INTO engine_object(class_name, gwkt) SELECT class_name, gwkt FROM tmp_x;
