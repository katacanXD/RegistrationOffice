import json
from pathlib import Path

path = Path(r"C:\Users\Katacan\Documents\RegistrationOffice\3 семак\tools\spec_tz.json")
spec = json.loads(path.read_text(encoding="utf-8"))
blocks = spec["blocks"]

i81 = next(i for i, b in enumerate(blocks) if b.get("text") == "8.1. Ограничения и допущения")
removed = blocks[i81:i81 + 2]
del blocks[i81:i81 + 2]
i_sources = next(i for i, b in enumerate(blocks) if str(b.get("text", "")).startswith("1) ГОСТ 34.602"))
blocks[i_sources + 1:i_sources + 1] = removed
path.write_text(json.dumps(spec, ensure_ascii=False, indent=2), encoding="utf-8")
print("moved", len(removed), "blocks after sources")
