import express from "express";

const app = express();
app.use(express.json({ limit: "64kb" }));

const PORT = Number(process.env.PORT || 8080);
const order = (process.env.PROVIDER_ORDER || "ignav,duffel,travelpayouts")
  .split(",").map(x => x.trim().toLowerCase()).filter(Boolean);

function validDate(x) { return /^\\d{4}-\\d{2}-\\d{2}$/.test(x || ""); }

function normalizeDuffelOffer(o) {
  const slice = o?.slices?.[0];
  const seg = slice?.segments?.[0];
  if (!slice || !seg) return null;
  const stops = Math.max(0, (slice.segments?.length || 1) - 1);
  const second = o?.slices?.[1];
  const mapSegments = (slice) => (slice?.segments || []).map(x => ({
    marketing_carrier_code: x.marketing_carrier?.iata_code || "",
    marketing_carrier_name: x.marketing_carrier?.name || x.operating_carrier?.name || "Airline",
    operating_carrier_name: x.operating_carrier?.name || "",
    flight_number: x.marketing_carrier_flight_number || x.flight_number || "",
    departure_airport: x.origin?.iata_code || "",
    arrival_airport: x.destination?.iata_code || "",
    departure_time_local: x.departing_at || "",
    arrival_time_local: x.arriving_at || ""
  }));
  return {
    provider: "Duffel", offerId: o.id || "",
    airline: seg.marketing_carrier?.name || seg.operating_carrier?.name || "Airline",
    carrier: seg.marketing_carrier?.iata_code || "",
    origin: seg.origin?.iata_code || "", destination: slice?.segments?.at(-1)?.destination?.iata_code || "",
    departAt: seg.departing_at || "", arriveAt: slice?.segments?.at(-1)?.arriving_at || "",
    durationMinutes: Math.round((Date.parse(slice?.segments?.at(-1)?.arriving_at) - Date.parse(seg.departing_at)) / 60000) || 0,
    stops, amount: o.total_amount || "", currency: o.total_currency || "USD",
    segments: mapSegments(slice),
    returnSegments: mapSegments(second),
    returnDepartAt: second?.segments?.[0]?.departing_at || "",
    returnArriveAt: second?.segments?.at(-1)?.arriving_at || "",
    returnDurationMinutes: second ? Math.round((Date.parse(second?.segments?.at(-1)?.arriving_at) - Date.parse(second?.segments?.[0]?.departing_at)) / 60000) || 0 : 0,
    returnStops: second ? Math.max(0, (second.segments?.length || 1) - 1) : 0,
    expiresAt: o.expires_at || ""
  };
}

async function duffel(req) {
  const token = process.env.DUFFEL_ACCESS_TOKEN;
  if (!token) throw new Error("DUFFEL_ACCESS_TOKEN is not configured");
  const body = {
    data: {
      slices: [{
        origin: req.origin,
        destination: req.destination,
        departure_date: req.departureDate
      }],
      passengers: [
        ...Array.from({length: Number(req.adults || 1)}, () => ({ type: "adult" })),
        ...Array.from({length: Number(req.children || 0)}, () => ({ type: "child" }))
      ],
      cabin_class: String(req.cabin || "Economy").toLowerCase().replace(" ", "_")
    }
  };
  if (req.tripType === "Round Trip" && req.returnDate) {
    body.data.slices.push({
      origin: req.destination,
      destination: req.origin,
      departure_date: req.returnDate
    });
  }
  const r = await fetch("https://api.duffel.com/air/offer_requests", {
    method: "POST",
    headers: {
      "Authorization": "Bearer " + token,
      "Duffel-Version": "v2",
      "Content-Type": "application/json"
    },
    body: JSON.stringify(body)
  });
  if (!r.ok) throw new Error("Duffel HTTP " + r.status);
  const j = await r.json();
  const offers = j?.data?.offers || [];
  return offers.map(normalizeDuffelOffer).filter(Boolean);
}

async function ignav(req) {
  const token = process.env.IGNAV_API_KEY;
  if (!token) throw new Error("IGNAV_API_KEY is not configured");

  const passengers = Math.max(1, Number(req.adults || 1));
  const body = {
    origin: req.origin,
    destination: req.destination,
    departure_date: req.departureDate,
    cabin_class: String(req.cabin || "Economy").toLowerCase().replace(" ", "_"),
    adults: passengers,
    children: Number(req.children || 0),
    market: "IL"
  };

  const path = req.tripType === "Round Trip" && req.returnDate ? "round-trip" : "one-way";
  if (path === "round-trip") body.return_date = req.returnDate;

  const r = await fetch("https://ignav.com/api/fares/" + path, {
    method: "POST",
    headers: {
      "X-Api-Key": token,
      "Content-Type": "application/json",
      "Accept": "application/json"
    },
    body: JSON.stringify(body)
  });
  if (!r.ok) throw new Error("Ignav HTTP " + r.status);

  const j = await r.json();
  return (j?.itineraries || []).map((it, index) => {
    const out = it?.outbound;
    const first = out?.segments?.[0];
    const last = out?.segments?.[out?.segments?.length - 1];
    const inbound = it?.inbound;
    const inFirst = inbound?.segments?.[0];
    const inLast = inbound?.segments?.[inbound?.segments?.length - 1];
    return {
      provider: "Ignav",
      offerId: it.ignav_id || String(index),
      airline: out?.carrier || first?.operating_carrier_name || "Airline",
      carrier: first?.marketing_carrier_code || "",
      origin: first?.departure_airport || req.origin,
      destination: last?.arrival_airport || req.destination,
      departAt: first?.departure_time_utc || first?.departure_time_local || "",
      arriveAt: last?.arrival_time_utc || last?.arrival_time_local || "",
      durationMinutes: Number(out?.duration_minutes || 0),
      stops: Math.max(0, (out?.segments?.length || 1) - 1),
      amount: String(it?.price?.amount ?? ""),
      currency: it?.price?.currency || "USD",
      returnDepartAt: inFirst ? (inFirst.departure_time_utc || inFirst.departure_time_local || "") : "",
      returnArriveAt: inLast ? (inLast.arrival_time_utc || inLast.arrival_time_local || "") : "",
      returnDurationMinutes: Number(inbound?.duration_minutes || 0),
      returnStops: inbound ? Math.max(0, (inbound?.segments?.length || 1) - 1) : 0,
      priceStatus: it?.price?.status || "unverified"
    };
  });
}

async function travelpayouts(req) {
  const token = process.env.TRAVELPAYOUTS_TOKEN;
  if (!token) throw new Error("TRAVELPAYOUTS_TOKEN is not configured");
  const u = new URL("https://api.travelpayouts.com/v1/prices/direct");
  u.searchParams.set("origin", req.origin);
  u.searchParams.set("destination", req.destination);
  u.searchParams.set("depart_date", req.departureDate);
  if (req.returnDate) u.searchParams.set("return_date", req.returnDate);
  u.searchParams.set("currency", "USD");
  const r = await fetch(u, { headers: { "X-Access-Token": token, "Accept": "application/json" } });
  if (!r.ok) throw new Error("Travelpayouts HTTP " + r.status);
  const j = await r.json();
  const rows = [];
  for (const v of Object.values(j?.data || {})) {
    if (!v) continue;
    rows.push({
      provider: "Travelpayouts",
      offerId: "",
      airline: v.airline || "Airline",
      carrier: v.airline || "",
      origin: req.origin,
      destination: req.destination,
      departAt: v.departure_at || "",
      arriveAt: "",
      durationMinutes: 0,
      stops: Number(v.transfers || 0),
      amount: String(v.price ?? ""),
      currency: j.currency || "USD"
    });
  }
  return rows;
}

const providers = { ignav, duffel, travelpayouts };

app.get("/health", (_req, res) => {
  res.json({
    ok: true,
    providers: order.map(name => ({
      name,
      configured: Boolean(
        name === "ignav" ? process.env.IGNAV_API_KEY :\n        name === "duffel" ? process.env.DUFFEL_ACCESS_TOKEN :
        name === "travelpayouts" ? process.env.TRAVELPAYOUTS_TOKEN : false
      )
    }))
  });
});

app.post("/v1/flights/seat-map", async (req, res) => {
  const token = process.env.DUFFEL_ACCESS_TOKEN;
  if (!token) return res.status(503).json({ error: "DUFFEL_ACCESS_TOKEN is not configured" });
  if (!req.body?.offerId) return res.status(400).json({ error: "offerId is required" });
  try {
    const r = await fetch("https://api.duffel.com/air/seat_maps?offer_id=" + encodeURIComponent(req.body.offerId), {
      headers: { "Authorization": "Bearer " + token, "Duffel-Version": "v2", "Accept": "application/json" }
    });
    const j = await r.json();
    if (!r.ok) return res.status(r.status).json(j);
    const seatMaps = (j?.data || []).map((m, i) => ({
      segment: String(i + 1),
      seats: (m?.cabins || []).flatMap(c => (c?.rows || []).flatMap(row => (row?.sections || []).flatMap(sec => (sec?.elements || []).filter(e => e?.type === "seat").map(e => ({
        designator: e.designator || e.id || "?",
        available: e.available === true,
        name: e.name || "",
        price: e.total_amount || "0",
        currency: e.total_currency || ""
      })))) )
    }));
    res.json({ seatMaps });
  } catch (e) { res.status(502).json({ error: e.message }); }
});

async function geocode(place) {
  const u = new URL("https://nominatim.openstreetmap.org/search");
  u.searchParams.set("q", place); u.searchParams.set("format", "json"); u.searchParams.set("limit", "1");
  const r = await fetch(u, { headers: { "User-Agent": "Vicationfly/1.0" } });
  const j = await r.json(); if (!j?.[0]) throw new Error("Destination could not be located");
  return { latitude: Number(j[0].lat), longitude: Number(j[0].lon) };
}

app.post("/v1/hotels/search", async (req, res) => {
  const token = process.env.DUFFEL_ACCESS_TOKEN;
  if (!token) return res.status(503).json({ error: "DUFFEL_ACCESS_TOKEN is not configured" });
  try {
    const q = req.body || {};
    const coords = q.latitude && q.longitude ? { latitude: Number(q.latitude), longitude: Number(q.longitude) } : await geocode(q.destination || "");
    const checkIn = q.checkIn; const checkOut = q.checkOut;
    if (!checkIn || !checkOut) return res.status(400).json({ error: "checkIn and checkOut are required" });
    const guests = [];
    for (let i=0;i<Math.max(1,Number(q.adults||1));i++) guests.push({type:"adult"});
    for (let i=0;i<Math.max(0,Number(q.children||0));i++) guests.push({type:"child"});
    const u = "https://api.duffel.com/stays/search";
    const payload = { data: { rooms: Math.max(1,Number(q.rooms||1)), mobile:true, location:{radius:100,geographic_coordinates:coords}, check_in_date:checkIn, check_out_date:checkOut, guests, instant_payment:true } };
    const r = await fetch(u,{method:"POST",headers:{"Authorization":"Bearer "+token,"Duffel-Version":"v2","Content-Type":"application/json"},body:JSON.stringify(payload)});
    const j = await r.json(); if(!r.ok) return res.status(r.status).json(j);
    const rows=(j?.data?.results||[]).map(x=>{const a=x.accommodation||{};const addr=a.location?.address||{};const photo=a.photos?.[0]?.url||"";return {searchResultId:x.id||"",name:a.name||"Hotel",photo,address:[addr.line_one,addr.postal_code].filter(Boolean).join(", "),city:addr.city_name||"",country:addr.country_code||"",description:a.description||"",rating:a.rating||0,reviewScore:a.review_score||0,price:x.cheapest_rate_total_amount||"",currency:x.cheapest_rate_currency||"USD",checkIn:x.check_in_date||checkIn,checkOut:x.check_out_date||checkOut,expiresAt:x.expires_at||""};});
    res.json({ hotels:rows, after:j?.meta?.after||null });
  } catch(e) { res.status(502).json({error:e.message}); }
});

app.post("/v1/hotels/rates", async (req,res)=>{
  const token=process.env.DUFFEL_ACCESS_TOKEN;if(!token)return res.status(503).json({error:"DUFFEL_ACCESS_TOKEN is not configured"});
  try{const id=req.body?.searchResultId;if(!id)return res.status(400).json({error:"searchResultId is required"});const r=await fetch("https://api.duffel.com/stays/search_results/"+encodeURIComponent(id)+"/actions/fetch_all_rates",{method:"POST",headers:{"Authorization":"Bearer "+token,"Duffel-Version":"v2","Content-Type":"application/json"}});const j=await r.json();if(!r.ok)return res.status(r.status).json(j);res.json(j);}catch(e){res.status(502).json({error:e.message});}
});

app.post("/v1/hotels/quote", async (req,res)=>{
  const token=process.env.DUFFEL_ACCESS_TOKEN;if(!token)return res.status(503).json({error:"DUFFEL_ACCESS_TOKEN is not configured"});
  try{const id=req.body?.rateId;if(!id)return res.status(400).json({error:"rateId is required"});const r=await fetch("https://api.duffel.com/stays/quotes",{method:"POST",headers:{"Authorization":"Bearer "+token,"Duffel-Version":"v2","Content-Type":"application/json"},body:JSON.stringify({data:{rate_id:id}})});const j=await r.json();if(!r.ok)return res.status(r.status).json(j);res.json(j);}catch(e){res.status(502).json({error:e.message});}
});

app.post("/v1/flights/search", async (req, res) => {
  const q = req.body || {};
  if (!q.origin || !q.destination || !validDate(q.departureDate)) {
    return res.status(400).json({ error: "origin, destination and a valid departureDate are required" });
  }
  if (q.tripType === "Round Trip" && !validDate(q.returnDate)) {
    return res.status(400).json({ error: "returnDate is required for round trips" });
  }

  const errors = [];
  for (const name of order) {
    const fn = providers[name];
    if (!fn) continue;
    try {
      const offers = await fn(q);
      if (offers.length) {
        return res.json({ offers, provider: name, fallbackUsed: name !== order[0] });
      }
      errors.push(name + ": no offers");
    } catch (e) {
      errors.push(name + ": " + e.message);
    }
  }
  res.status(503).json({
    error: "No configured provider returned live/cached offers.",
    providersTried: order,
    details: errors
  });
});

app.listen(PORT, () => console.log("Vicationfly flight gateway listening on " + PORT));
