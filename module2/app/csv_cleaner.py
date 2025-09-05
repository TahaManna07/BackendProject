from __future__ import annotations

import json
import re
from typing import Dict, List, Tuple

import pandas as pd


# Normalisation des noms de colonnes: lowercase, underscores, corrections communes
_COLUMN_FIXES = {
    "proce": "price",
    "descption": "description",
    "imageurl": "image_url",
    "image url": "image_url",
    "uaitity": "quantity",
    "catgroy": "category",
    "categorie": "category",
    "img_url": "image_url",
}


def _normalize_col_name(name: str) -> str:
    base = re.sub(r"[^A-Za-z0-9]+", "_", name.strip().lower()).strip("_")
    return _COLUMN_FIXES.get(base, base)


def _normalize_columns(df: pd.DataFrame) -> pd.DataFrame:
    df = df.copy()
    df.columns = [_normalize_col_name(c) for c in df.columns]
    return df


def _trim_str_columns(df: pd.DataFrame) -> pd.DataFrame:
    df = df.copy()
    obj_cols = df.select_dtypes(include=["object", "string"]).columns
    for c in obj_cols:
        df[c] = df[c].apply(lambda v: v.strip() if isinstance(v, str) else v)
    return df


def process_csv(input_path: str, output_path: str, id_column: str = "id") -> Dict:
    df = pd.read_csv(input_path)
    df = _normalize_columns(df)
    df = _trim_str_columns(df)

    total_rows = int(len(df))
    null_cells_count = int(df.isna().sum().sum())

    if id_column in df.columns:
        duplicates_count = int(df.duplicated(subset=[id_column], keep="first").sum())
        df_clean = df.drop_duplicates(subset=[id_column], keep="first")
    else:
        duplicates_count = int(df.duplicated(keep="first").sum())
        df_clean = df.drop_duplicates(keep="first")

    cleaned_rows = int(len(df_clean))
    duplicates_removed = int(total_rows - cleaned_rows)

    # Écrit le CSV nettoyé
    df_clean.to_csv(output_path, index=False)

    report = {
        "input_file": input_path,
        "output_file": output_path,
        "id_column_used": id_column if id_column in df.columns else None,
        "total_rows": total_rows,
        "cleaned_rows": cleaned_rows,
        "duplicates_detected": duplicates_count,
        "duplicates_removed": duplicates_removed,
        "null_cells": null_cells_count,
        "columns": list(df.columns),
    }
    return report


def report_to_json(report: Dict, pretty: bool = True) -> str:
    return json.dumps(report, ensure_ascii=False, indent=2 if pretty else None)
