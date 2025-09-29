const dec2 = /^-?\d+(\.\d{1,2})?$/; // десятичные числа с точкой, до 2 знаков

const limits = {
  x: { min: -5, max: 3 },
  y: { min: -3, max: 5 },
  r: ["1", "2", "3", "4", "5"]
};

function setError(inputEl, errEl, message) {
  inputEl?.classList.add("is-invalid");
  if (errEl) errEl.textContent = message || "";
}

function clearError(inputEl, errEl) {
  inputEl?.classList.remove("is-invalid");
  if (errEl) errEl.textContent = "";
}

function validateNumberField(id, errId, { min, max }) {
  const el = document.getElementById(id);
  const err = document.getElementById(errId);
  const raw = (el.value || "").trim();

  // пусто
  if (!raw) {
    setError(el, err, "Поле не должно быть пустым");
    return { ok: false };
  }
  // формат
  if (!dec2.test(raw)) {
    setError(el, err, "Неверный формат. Используйте точку и до 2 знаков");
    return { ok: false };
  }
  const num = Number(raw);
  // диапазон
  if (num < min || num > max) {
    setError(el, err, `Допустимо от ${min} до ${max}`);
    return { ok: false };
  }

  clearError(el, err);
  return { ok: true, value: num.toFixed(2) };
}

function validateR() {
  const radios = document.querySelectorAll("input[name='r']");
  const err = document.getElementById("rErr");

  let checked = null;
  radios.forEach(r => { if (r.checked) checked = r.value; });

  if (!checked || !limits.r.includes(checked)) {
    err.textContent = "Выберите одно из значений R (1–5)";
    return { ok: false };
  } else {
    err.textContent = "";
    document.querySelectorAll("#pointForm td > label").forEach(l => {
      l.style.outline = "none";
    });
    return { ok: true, value: checked };
  }
}

function addToHistory(record) {
  const tbody = document.querySelector("#historyTable tbody");
  const tr = document.createElement("tr");
  tr.innerHTML = `
    <td>${record.x}</td>
    <td>${record.y}</td>
    <td>${record.r}</td>
    <td>${record.hit}</td>
    <td>${record.now}</td>
    <td>${record.execTime}</td>
  `;
  tbody.appendChild(tr);
}

function saveHistory() {
  const rows = document.querySelectorAll("#historyTable tbody tr");
  const data = Array.from(rows).map(row => {
    const c = row.querySelectorAll("td");
    return {
      x: c[0].textContent, y: c[1].textContent, r: c[2].textContent,
      hit: c[3].textContent, now: c[4].textContent, execTime: c[5].textContent
    };
  });
  localStorage.setItem("history", JSON.stringify(data));
}

function loadHistory() {
  const data = JSON.parse(localStorage.getItem("history") || "[]");
  data.forEach(addToHistory);
}

function clearHistory() {
  // очистка таблицы и localStorage
  document.querySelector("#historyTable tbody").innerHTML = "";
  localStorage.removeItem("history");
}

function clearErrors() {
  // сообщения об ошибках под полями
  document.querySelectorAll(".error-msg").forEach(el => (el.textContent = ""));
  // красная рамка у инпутов
  document.querySelectorAll("input.is-invalid").forEach(el => el.classList.remove("is-invalid"));
  // контуры у лейблов R
  document.querySelectorAll("#pointForm td > label").forEach(l => (l.style.outline = "none"));
}

function handleSubmit(e) {
  e.preventDefault();

  const vX = validateNumberField("x", "xErr", limits.x);
  const vY = validateNumberField("y", "yErr", limits.y);
  const vR = validateR();

  if (!vX.ok || !vY.ok || !vR.ok) {
    if (!vX.ok) document.getElementById("x").focus();
    else if (!vY.ok) document.getElementById("y").focus();
    else document.querySelector("input[name='r']").focus();
    return;
  }

  const x = vX.value;
  const y = vY.value;
  const r = vR.value;

  // TODO: тут будет реальный запрос к FastCGI через fetch GET
  // Пока имитация ответа сервера:
  const hit = Math.random() > 0.5 ? "Да" : "Нет";
  const now = new Date().toLocaleString();
  const execTime = Math.floor(Math.random() * 10) + " мс";

  const rec = { x, y, r, hit, now, execTime };
  addToHistory(rec);
  saveHistory();
}

document.addEventListener("DOMContentLoaded", () => {
  loadHistory();

  const form = document.getElementById("pointForm");
  form.addEventListener("submit", handleSubmit);

  document.getElementById("x").addEventListener("input", () => validateNumberField("x", "xErr", limits.x));
  document.getElementById("y").addEventListener("input", () => validateNumberField("y", "yErr", limits.y));
  document.querySelectorAll("input[name='r']").forEach(r => {
    r.addEventListener("change", validateR);
  });

  const clearBtn = document.querySelector("button[onclick='clearHistory()']");
  if (clearBtn) clearBtn.addEventListener("click", clearHistory);

  form.addEventListener("reset", () => {
    clearErrors();
  });
});
