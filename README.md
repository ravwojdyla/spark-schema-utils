# spark-schema-utils

## Raison d'être

See [stackoverflow#71610435](https://stackoverflow.com/questions/71610435/how-to-overwrite-pyspark-dataframe-schema-without-data-scan/).

## PySpark usage

```python
from pyspark.sql import SparkSession
from pyspark.sql import Row, DataFrame
from pyspark.sql.types import StructType


# These are generated/specified somewhere
schema_wo_metadata: StructType = ...
schema_wi_metadata: StructType = ...

# You need to include this package
spark = SparkSession.builder \
    .config("spark.jars.packages", "io.github.ravwojdyla:spark-schema-utils_2.12:0.1.0") \
    .getOrCreate()

# Dummy data with `schema_wo_metadata` schema:
df = spark.createDataFrame(data=[Row(oa=[Row(ia=0, ib=1)], ob=3.14),
                                 Row(oa=[Row(ia=2, ib=3)], ob=42.0)],
                           schema=schema_wo_metadata)
assert df.schema == schema_wo_metadata

_jdf = spark._sc._jvm.io.github.ravwojdyla.SchemaUtils.update(df._jdf, schema.json())
new_df = DataFrame(_jdf, df.sql_ctx)

assert new_df.schema != schema_wo_metadata
assert new_df.schema == schema_wi_metadata
```

## License

Copyright 2022 Rafal Wojdyla

Licensed under the Apache License, Version 2.0: http://www.apache.org/licenses/LICENSE-2.0
