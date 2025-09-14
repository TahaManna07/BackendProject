from __future__ import annotations
import json
import re
from typing import Any, Dict, List, Tuple

import pandas as pd

# Product schema (keep order)
EXPECTED_COLUMNS: List[str] = [
    "id", "name", "price", "description", "image_url",
    "quantity", "category", "sku", "available", "discount",
]

# Common header typos/synonyms -> normalized names
_COLUMN_FIXES = {
    "proce": "price",
    "descption": "description",
    "imageurl": "image_url",
    "image url": "image_url",
    "uaitity": "quantity",
    "catgroy": "category",
    "categorie": "category",
    "img_url": "image_url",
    "is_available": "available",
    "disponible": "available",
    "remise": "discount",
}

def _normalize_col_name(name: str) -> str:
    base = re.sub(r"[^A-Za-z0-9]+", "_", name.strip().lower()).strip("_")
    return _COLUMN_FIXES.get(base, base)

def _normalize_columns(df: pd.DataFrame) -> Tuple[pd.DataFrame, List[str]]:
    df = df.copy()
    normalized = [_normalize_col_name(c) for c in df.columns]
    df.columns = normalized
    return df, normalized

def _trim_str_columns(df: pd.DataFrame) -> pd.DataFrame:
    df = df.copy()
    obj_cols = df.select_dtypes(include=["object", "string"]).columns
    for c in obj_cols:
        df[c] = df[c].apply(lambda v: v.strip() if isinstance(v, str) else v)
    return df

def _to_bool(v: Any) -> Any:
    if v is None or (isinstance(v, float) and pd.isna(v)):
        return None
    if isinstance(v, bool):
        return v
    s = str(v).strip().lower()
    if s in {"1", "true", "yes", "y"}:
        return True
    if s in {"0", "false", "no", "n"}:
        return False
    return None

def _coerce_types(df: pd.DataFrame) -> pd.DataFrame:
    df = df.copy()
    # numeric
    for col in ("price", "discount"):
        if col in df.columns:
            df[col] = pd.to_numeric(df[col], errors="coerce")
    if "quantity" in df.columns:
        df["quantity"] = pd.to_numeric(df["quantity"], errors="coerce").astype("Int64")
    # boolean
    if "available" in df.columns:
        df["available"] = df["available"].apply(_to_bool)
    # keep sku, name, description, category, image_url as strings
    return df

def _align_to_schema(df: pd.DataFrame) -> Tuple[pd.DataFrame, List[str]]:
    df = df.copy()
    added: List[str] = []
    for col in EXPECTED_COLUMNS:
        if col not in df.columns:
            df[col] = None
            added.append(col)
    # reorder columns to schema-first (keep any extras at the end)
    ordered = [c for c in EXPECTED_COLUMNS] + [c for c in df.columns if c not in EXPECTED_COLUMNS]
    df = df[ordered]
    return df, added

def process_csv(input_path: str, output_path: str, id_column: str = "id") -> Dict:
    # Read with BOM-tolerant default
    df = pd.read_csv(input_path, encoding="utf-8-sig")
    df, normalized_cols = _normalize_columns(df)
    df = _trim_str_columns(df)
    df = _coerce_types(df)
    df, added_cols = _align_to_schema(df)

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

    # Ensure expected schema/order present in output
    df_clean, _ = _align_to_schema(df_clean)
    df_clean.to_csv(output_path, index=False, encoding="utf-8")

    report = {
        "input_file": input_path,
        "output_file": output_path,
        "id_column_used": id_column if id_column in df.columns else None,
        "total_rows": total_rows,
        "cleaned_rows": cleaned_rows,
        "duplicates_detected": duplicates_count,
        "duplicates_removed": duplicates_removed,
        "null_cells": null_cells_count,
        "present_columns": list(df.columns),
        "normalized_columns": normalized_cols,
        "added_missing_columns": added_cols,
        "expected_columns": EXPECTED_COLUMNS,
    }
    return report

def report_to_json(report: Dict, pretty: bool = True) -> str:
    return json.dumps(report, ensure_ascii=False, indent=2 if pretty else None)