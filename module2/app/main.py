import argparse
import sys
from pathlib import Path

# Support both "python -m module2.app.main" and "python module2/app/main.py"
try:
    from .csv_cleaner import process_csv, report_to_json
except ImportError:
    from csv_cleaner import process_csv, report_to_json  # type: ignore


def parse_args() -> argparse.Namespace:
    p = argparse.ArgumentParser(description="CSV cleaner (pandas): remove duplicates, trim strings, report stats.")
    p.add_argument("-i", "--input", required=True, help="Input CSV path")
    p.add_argument("-o", "--output", required=True, help="Output cleaned CSV path")
    p.add_argument("--id-column", default="id", help="Column to detect duplicates (default: id)")
    p.add_argument("-r", "--report", help="Optional JSON report path (also printed to stdout)")
    return p.parse_args()


def main() -> int:
    args = parse_args()
    try:
        in_path = Path(args.input)
        out_path = Path(args.output)
        if not in_path.exists():
            print(f"Input file not found: {in_path}", file=sys.stderr)
            return 1
        out_path.parent.mkdir(parents=True, exist_ok=True)

        report = process_csv(str(in_path), str(out_path), id_column=args.id_column)
        report_json = report_to_json(report)

        # Print JSON report to stdout
        print(report_json)

        # Optionally write report to file
        if args.report:
            rp = Path(args.report)
            rp.parent.mkdir(parents=True, exist_ok=True)
            rp.write_text(report_json, encoding="utf-8")

        return 0
    except Exception as e:
        print(f"Error: {e}", file=sys.stderr)
        return 1


if __name__ == "__main__":
    raise SystemExit(main())
