#!/usr/bin/env python3
"""
Builds app/src/main/assets/history.json: for every calendar day (MM-DD) a short, mixed list of
  b = births, d = deaths, e = historical events, s = special days / observances.
Source: the Bangla Wikipedia day pages ("৪ নভেম্বর"); when a Bangla page is thin, the English Wikipedia
"on this day" feed fills the gaps. Used once at build time; the app itself works offline.
Text is from Wikipedia (CC BY-SA); the app shows "উৎস: উইকিপিডিয়া".

usage: fetch_history.py OUTPUT.json        |   fetch_history.py --selftest
"""
import json, re, sys, time, urllib.parse, urllib.request, os, random

UA = "BanglaPanjikaBuild/1.0 (personal offline almanac app; contact via GitHub repo bangla-panjika)"
MONTHS = ["জানুয়ারি", "ফেব্রুয়ারি", "মার্চ", "এপ্রিল", "মে", "জুন", "জুলাই", "আগস্ট", "সেপ্টেম্বর", "অক্টোবর", "নভেম্বর", "ডিসেম্বর"]
DAYS = [31, 29, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31]
LIMIT = {"b": 8, "d": 8, "e": 8, "s": 6}
MIN_BN = {"b": 4, "d": 4, "e": 4, "s": 1}
BN_DIG = "০১২৩৪৫৬৭৮৯"
BENGAL_WORDS = ["বাংলা", "বাঙালি", "বাঙ্গালি", "কলকাতা", "কলিকাতা", "ঢাকা", "ভারত", "পশ্চিমবঙ্গ", "বঙ্গ", "বাংলাদেশ", "চট্টগ্রাম", "সাহিত্যিক", "কবি", "বিজ্ঞানী"]


def bn_num(n):
    return "".join(BN_DIG[int(c)] for c in str(n))


def en_num(s):
    return int("".join(str(BN_DIG.index(c)) if c in BN_DIG else c for c in s if c in BN_DIG or c.isdigit()) or 0)


def http_get(url, tries=4):
    last = None
    for i in range(tries):
        try:
            req = urllib.request.Request(url, headers={"User-Agent": UA, "Accept": "application/json"})
            with urllib.request.urlopen(req, timeout=30) as r:
                return r.read().decode("utf-8")
        except Exception as e:                       # network hiccup / rate limit: wait and retry
            last = e
            time.sleep(1.5 * (i + 1))
    raise last


# ------------------------------------------------------------------ wikitext cleaning
def strip_templates(s):
    prev = None
    while prev != s:
        prev = s
        s = re.sub(r"\{\{[^{}]*\}\}", "", s)
    return s


def clean(s):
    s = re.sub(r"<!--.*?-->", "", s, flags=re.S)
    s = re.sub(r"<ref[^>]*/>", "", s)
    s = re.sub(r"<ref[^>]*>.*?</ref>", "", s, flags=re.S)
    s = strip_templates(s)
    s = re.sub(r"\[\[(?:ফাইল|File|চিত্র|Image|বিষয়শ্রেণী|Category):[^\]]*\]\]", "", s, flags=re.I)
    s = re.sub(r"\[\[([^\]|]*)\|([^\]]*)\]\]", r"\2", s)
    s = re.sub(r"\[\[([^\]]*)\]\]", r"\1", s)
    s = re.sub(r"\[https?://\S+\s+([^\]]*)\]", r"\1", s)
    s = re.sub(r"\[https?://\S+\]", "", s)
    s = re.sub(r"<[^>]+>", "", s)
    s = s.replace("'''", "").replace("''", "").replace("&nbsp;", " ")
    s = re.sub(r"\s+", " ", s).strip(" -–—,;:")
    return s.strip()


def first_link(s):
    for m in re.finditer(r"\[\[([^\]|#]+)(?:#[^\]|]*)?(?:\|[^\]]*)?\]\]", s):
        t = m.group(1).strip()
        if re.match(r"^(ফাইল|File|চিত্র|Image|বিষয়শ্রেণী|Category|[\d০-৯])", t, re.I):
            continue
        if ":" in t:
            continue
        return t
    return None


def wiki_url(title, lang="bn"):
    return "https://%s.wikipedia.org/wiki/%s" % (lang, urllib.parse.quote(title.replace(" ", "_")))


def classify(h2):
    if "ঘটনা" in h2: return "e"
    if "জন্ম" in h2: return "b"
    if "মৃত্যু" in h2 or "প্রয়াণ" in h2: return "d"
    if "ছুটি" in h2 or "অনুষ্ঠান" in h2 or "উদ্‌যাপন" in h2 or "উদযাপন" in h2 or "দিবস" in h2: return "s"
    return None


YEAR_RE = re.compile(r"^(?:\[\[)?\s*([০-৯0-9]+)(?:\s*(খ্রিস্টপূর্বাব্দ|খ্রিস্টপূর্ব|খ্রি\.পূ\.|খ্রিষ্টপূর্বাব্দ))?[^\]\-–—:]*?(?:\]\])?\s*[–—\-:]\s*(.+)$")


def parse_wikitext(text):
    """-> {'b':[...], 'd':[...], 'e':[...], 's':[...]} items: {'y': 'bangla year text', 'n': int, 't': text, 'u': url|None}"""
    out = {"b": [], "d": [], "e": [], "s": []}
    cur = None
    for raw in text.splitlines():
        m = re.match(r"^(={2,6})\s*(.+?)\s*\1\s*$", raw)
        if m:
            if len(m.group(1)) == 2:
                cur = classify(clean(m.group(2)))
            continue
        if cur is None or not raw.startswith("*"):
            continue
        body = raw.lstrip("*").strip()
        if not body:
            continue
        if cur == "s":
            t = clean(body)
            if len(t) >= 6:
                out["s"].append({"y": "", "n": 0, "t": t, "u": None})
            continue
        ym = YEAR_RE.match(body)
        if not ym:
            continue
        year_txt = ym.group(1)
        bc = bool(ym.group(2))
        rest = ym.group(3)
        t = clean(rest)
        if len(t) < 8:
            continue
        link = first_link(rest)
        n = en_num(year_txt) * (-1 if bc else 1)
        y = (bn_num(en_num(year_txt)) + (" খ্রিস্টপূর্বাব্দ" if bc else ""))
        out[cur].append({"y": y, "n": n, "t": t, "u": wiki_url(link) if link else None})
    return out


# ------------------------------------------------------------------ English fallback
def parse_en_feed(data):
    out = {"b": [], "d": [], "e": [], "s": []}
    for key, k in (("births", "b"), ("deaths", "d"), ("events", "e"), ("holidays", "s")):
        for it in data.get(key, []) or []:
            t = (it.get("text") or "").strip()
            if len(t) < 8:
                continue
            u = None
            for p in it.get("pages") or []:
                u = ((p.get("content_urls") or {}).get("desktop") or {}).get("page")
                if u:
                    break
            yr = it.get("year")
            out[k].append({"y": bn_num(abs(yr)) + (" খ্রিস্টপূর্বাব্দ" if yr and yr < 0 else "") if isinstance(yr, int) else "",
                           "n": yr if isinstance(yr, int) else 0, "t": t, "u": u})
    return out


# ------------------------------------------------------------------ selection
def score(it):
    t = it["t"]
    sc = 0
    for w in BENGAL_WORDS:
        if w in t:
            sc += 3 if w in BENGAL_WORDS[:11] else 1
    if it.get("u"): sc += 1
    if 25 <= len(t) <= 260: sc += 1
    if len(t) > 400: sc -= 2
    if re.search(r"[A-Za-z]{4,}", t): sc -= 1          # English text inside Bangla lines reads badly
    return sc


def pick(items, k, seed):
    seen, uniq = set(), []
    for it in items:
        key = re.sub(r"\W+", "", it["t"])[:60]
        if key in seen:
            continue
        seen.add(key)
        uniq.append(it)
    rnd = random.Random(seed)
    uniq.sort(key=lambda it: (-score(it), rnd.random()))
    chosen = uniq[:k]
    if chosen and "n" in chosen[0]:
        chosen.sort(key=lambda it: it.get("n", 0))
    return chosen


def build_day(mm, dd, bn_items, en_items):
    day = {}
    for k in "bdes":
        items = list(bn_items.get(k, []))
        if len(items) < MIN_BN[k]:
            items += [dict(i, en=True) for i in en_items.get(k, [])]
        picked = pick(items, LIMIT[k], mm * 100 + dd)
        day[k] = [{"y": i["y"], "t": i["t"], **({"u": i["u"]} if i.get("u") else {})} for i in picked]
    return day


# ------------------------------------------------------------------ main
def fetch_bn(mm, dd):
    title = "%s %s" % (bn_num(dd), MONTHS[mm - 1])
    url = ("https://bn.wikipedia.org/w/api.php?action=parse&format=json&formatversion=2&redirects=1&prop=wikitext&page="
           + urllib.parse.quote(title))
    data = json.loads(http_get(url))
    return parse_wikitext((data.get("parse") or {}).get("wikitext") or "")


def fetch_en(mm, dd):
    url = "https://api.wikimedia.org/feed/v1/wikipedia/en/onthisday/all/%02d/%02d" % (mm, dd)
    return parse_en_feed(json.loads(http_get(url)))


def diag(mm, dd):
    """print what the Bangla page looks like, so a wrong guess about its layout can be fixed from the log"""
    title = "%s %s" % (bn_num(dd), MONTHS[mm - 1])
    url = ("https://bn.wikipedia.org/w/api.php?action=parse&format=json&formatversion=2&redirects=1&prop=wikitext&page="
           + urllib.parse.quote(title))
    try:
        raw = http_get(url)
        data = json.loads(raw)
        wt = (data.get("parse") or {}).get("wikitext") or ""
        print("DIAG %s: title=%r wikitext=%d chars, keys=%s" % ("%02d-%02d" % (mm, dd), title, len(wt), list(data.keys())))
        if not wt:
            print("DIAG raw start:", raw[:400])
        print("DIAG headings:", [l.strip() for l in wt.splitlines() if l.startswith("=")][:40])
        r = parse_wikitext(wt)
        print("DIAG parsed counts:", {k: len(v) for k, v in r.items()})
        print("DIAG wikitext start:", wt[:700].replace("\n", " | "))
        bullets = [l for l in wt.splitlines() if l.startswith("*")][:6]
        print("DIAG first bullets:", bullets)
    except Exception as e:
        print("DIAG failed %02d-%02d: %s" % (mm, dd, e))


def one_day(mm, dd):
    bn_items = {"b": [], "d": [], "e": [], "s": []}
    en_items = {"b": [], "d": [], "e": [], "s": []}
    msgs = []
    try:
        bn_items = fetch_bn(mm, dd)
    except Exception as e:
        msgs.append("bn fail %02d-%02d: %s" % (mm, dd, e))
    if any(len(bn_items[k]) < MIN_BN[k] for k in "bdes"):
        try:
            en_items = fetch_en(mm, dd)
        except Exception as e:
            msgs.append("en fail %02d-%02d: %s" % (mm, dd, e))
    time.sleep(0.1)
    return mm, dd, build_day(mm, dd, bn_items, en_items), len(bn_items["b"]) + len(bn_items["d"]) + len(bn_items["e"]), msgs


def main(outpath):
    from concurrent.futures import ThreadPoolExecutor
    print("start", time.strftime("%H:%M:%S"))
    for (m, d) in ((1, 1), (11, 4)):
        diag(m, d)
    jobs = [(mm, dd) for mm in range(1, 13) for dd in range(1, DAYS[mm - 1] + 1)]
    result, thin, bn_total = {}, [], 0
    with ThreadPoolExecutor(max_workers=5) as ex:
        for mm, dd, day, nbn, msgs in ex.map(lambda a: one_day(*a), jobs):
            for m in msgs[:1]:
                print(m)
            bn_total += nbn
            if any(len(day[k]) == 0 for k in "bde"):
                thin.append("%02d-%02d" % (mm, dd))
            result["%02d-%02d" % (mm, dd)] = day
    total = {k: sum(len(v[k]) for v in result.values()) for k in "bdes"}
    print("items from the Bangla pages (b+d+e):", bn_total)
    print("totals chosen:", total, "days with an empty b/d/e list:", len(thin), thin[:20])
    print("end", time.strftime("%H:%M:%S"))
    if sum(total[k] for k in "bde") < 800:                       # almost nothing came through: do not write a useless file
        print("too little data fetched - not writing the file")
        return 1
    os.makedirs(os.path.dirname(os.path.abspath(outpath)), exist_ok=True)
    with open(outpath, "w", encoding="utf-8") as f:
        json.dump(result, f, ensure_ascii=False, separators=(",", ":"))
    print("sample 11-04:", json.dumps(result.get("11-04"), ensure_ascii=False)[:1200])
    return 0


def selftest():
    sample = """{{ঢালাই}}
== ঘটনাবলি ==
=== ২০শ শতক ===
* [[১৯৪৮]] – [[কলকাতা]]য় একটি সভা অনুষ্ঠিত হয়।<ref>x</ref>
*[[১৯৯০]] - ইরাক '''কুয়েত''' আক্রমণ করে।
== জন্ম ==
* [[১৮৬১]] - [[রবীন্দ্রনাথ ঠাকুর]], বাঙালি কবি ও সাহিত্যিক।
* ১৯২০ – [[আইজাক আসিমভ]], রুশ-মার্কিন লেখক।
== মৃত্যু ==
* [[১৯৫২]] – [[অমুক ব্যক্তি]], বিখ্যাত বিজ্ঞানী ও গবেষক।
== ছুটি ও অনুষ্ঠান ==
* [[বিশ্ব দিবস]]
** জাতীয় দিবস (দেশ)
"""
    r = parse_wikitext(sample)
    assert len(r["e"]) == 2 and r["e"][0]["y"] == "১৯৪৮" and "অনুষ্ঠিত" in r["e"][0]["t"] and "ref" not in r["e"][0]["t"], r["e"]
    assert r["e"][0]["u"].endswith("%E0%A6%95%E0%A6%B2%E0%A6%95%E0%A6%BE%E0%A6%A4%E0%A6%BE"), r["e"][0]
    assert len(r["b"]) == 2 and r["b"][0]["u"] and r["b"][1]["n"] == 1920, r["b"]
    assert len(r["d"]) == 1 and len(r["s"]) == 2, r
    en = parse_en_feed({"births": [{"text": "Isaac Asimov, writer", "year": 1920, "pages": [{"content_urls": {"desktop": {"page": "https://en.wikipedia.org/wiki/Isaac_Asimov"}}}]}]})
    assert en["b"][0]["y"] == "১৯২০" and en["b"][0]["u"].endswith("Isaac_Asimov"), en
    day = build_day(1, 2, r, en)
    assert len(day["b"]) == 3 and day["e"][0]["y"], day
    json.dumps(day, ensure_ascii=False)
    print("selftest ok")
    return 0


if __name__ == "__main__":
    sys.exit(selftest() if len(sys.argv) > 1 and sys.argv[1] == "--selftest" else main(sys.argv[1]))
