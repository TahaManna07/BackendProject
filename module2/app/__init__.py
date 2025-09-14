from fastapi import FastAPI, UploadFile, File, Form
from fastapi.responses import StreamingResponse
from pathlib import Path
import io, tempfile, zipfile

from .csv_cleaner import process_csv, report_to_json

app = FastAPI(title="CSV Cleaner Service", version="1.0.0")

@app.get("/health")
def health():
    return {"status": "ok"}

@app.post("/process", response_class=StreamingResponse)
async def process_endpoint(
    file: UploadFile = File(...),
    id_column: str = Form("id"),
):
    # Save upload to temp
    with tempfile.TemporaryDirectory() as tmpdir:
        tmp = Path(tmpdir)
        in_path = tmp / (file.filename or "products.csv")
        data = await file.read()
        in_path.write_bytes(data)

        out_csv = tmp / "clean.csv"
        report = process_csv(str(in_path), str(out_csv), id_column=id_column)
        report_json = report_to_json(report)

        # Build ZIP in memory
        buf = io.BytesIO()
        with zipfile.ZipFile(buf, "w", compression=zipfile.ZIP_DEFLATED) as z:
            z.write(out_csv, arcname="clean.csv")
            z.writestr("report.json", report_json)
        buf.seek(0)

        headers = {"Content-Disposition": 'attachment; filename="processed.zip"'}
        return StreamingResponse(buf, media_type="application/zip", headers=headers)