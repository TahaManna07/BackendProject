import argparse
import sys
import time
from pathlib import Path
from shutil import move

# Support both "python -m module2.app.main" and "python module2/app/main.py"
try:
    from .csv_cleaner import process_csv, report_to_json
except ImportError:
    from csv_cleaner import process_csv, report_to_json  # type: ignore


def parse_args() -> argparse.Namespace:
    p = argparse.ArgumentParser(description="CSV cleaner (pandas): remove duplicates, trim strings, report stats.")
    # Single-run mode
    p.add_argument("-i", "--input", help="Input CSV path")
    p.add_argument("-o", "--output", help="Output cleaned CSV path")
    p.add_argument("-r", "--report", help="Optional JSON report path (also printed to stdout)")
    # Common
    p.add_argument("--id-column", default="id", help="Column to detect duplicates (default: id)")
    # Watch mode
    p.add_argument("--watch", action="store_true", help="Watch input-dir and process new CSV files")
    p.add_argument("--input-dir", help="Folder to watch for CSV files (watch mode)")
    p.add_argument("--output-dir", help="Folder to write cleaned CSV/report (watch mode)")
    p.add_argument("--archive-dir", help="Folder to move processed originals (default: <input-dir>/processed)")
    p.add_argument("--interval", type=int, default=20, help="Polling interval in seconds (default: 20)")
    return p.parse_args()


def _process_one_file(in_file: Path, out_dir: Path, id_column: str) -> tuple[Path, Path, str]:
    base = in_file.stem
    out_csv = out_dir / f"{base}_clean.csv"
    out_json = out_dir / f"{base}_report.json"
    out_dir.mkdir(parents=True, exist_ok=True)

    report = process_csv(str(in_file), str(out_csv), id_column=id_column)
    report_json = report_to_json(report)
    out_json.write_text(report_json, encoding="utf-8")
    # Also print JSON to stdout for log/consumers
    print(report_json, flush=True)
    return out_csv, out_json, report_json


def _run_watch(input_dir: Path, output_dir: Path, archive_dir: Path, id_column: str, interval: int) -> int:
    input_dir.mkdir(parents=True, exist_ok=True)
    output_dir.mkdir(parents=True, exist_ok=True)
    archive_dir.mkdir(parents=True, exist_ok=True)

    print(f"[watch] Listening on {input_dir} -> {output_dir} (archive: {archive_dir}) every {interval}s", file=sys.stderr)
    try:
        while True:
            found = False
            for f in sorted(input_dir.glob("*.csv")):
                # Skip temp files
                if f.name.startswith("~$"):
                    continue
                found = True
                try:
                    out_csv, out_json, _ = _process_one_file(f, output_dir, id_column)
                    # Move original to archive
                    dest = archive_dir / f.name
                    if dest.exists():
                        dest.unlink()
                    move(str(f), str(dest))
                    print(f"[watch] Processed {f.name} -> {out_csv.name}, {out_json.name}", file=sys.stderr)
                except Exception as e:
                    print(f"[watch] Error processing {f.name}: {e}", file=sys.stderr)
            time.sleep(interval if not found else 0 if interval < 1 else interval)
    except KeyboardInterrupt:
        print("[watch] Stopped.", file=sys.stderr)
        return 0


def main() -> int:
    args = parse_args()
    # Watch mode
    if args.watch:
        if not args.input_dir or not args.output_dir:
            print("Error: --input-dir and --output-dir are required in --watch mode", file=sys.stderr)
            return 2
        in_dir = Path(args.input_dir)
        out_dir = Path(args.output_dir)
        arc_dir = Path(args.archive_dir) if args.archive_dir else in_dir / "processed"
        return _run_watch(in_dir, out_dir, arc_dir, args.id_column, args.interval)

    # Single-run mode (backward compatible)
    if not args.input or not args.output:
        print("Error: --input and --output are required (or use --watch)", file=sys.stderr)
        return 2
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