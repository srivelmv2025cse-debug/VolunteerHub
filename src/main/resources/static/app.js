(() => {
    const apiBase = "";
    const state = { events: [], upcoming: [], volunteers: [], summary: null };
    const byId = (id) => document.getElementById(id);

    const escapeHtml = (value = "") => String(value).replace(/[&<>"']/g, (character) => ({
        "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;"
    })[character]);

    async function request(path, options = {}) {
        const response = await fetch(`${apiBase}${path}`, {
            ...options,
            headers: { ...(options.body ? { "Content-Type": "application/json" } : {}), ...options.headers }
        });
        const contentType = response.headers.get("content-type") || "";
        const body = contentType.includes("json") ? await response.json() : await response.text();
        if (!response.ok) {
            throw new Error(body && typeof body === "object" ? body.message : "The request could not be completed.");
        }
        return body;
    }

    function showNotice(message, isError = false) {
        const notice = byId("notice");
        notice.textContent = message;
        notice.classList.toggle("error", isError);
        notice.hidden = false;
        window.clearTimeout(showNotice.timer);
        showNotice.timer = window.setTimeout(() => { notice.hidden = true; }, 5200);
    }

    function initials(name = "") {
        return name.trim().split(/\s+/).slice(0, 2).map((part) => part[0] || "").join("").toUpperCase();
    }

    function formatDate(value, options = { month: "short", day: "numeric", year: "numeric" }) {
        if (!value) return "Date not set";
        return new Intl.DateTimeFormat(undefined, options).format(new Date(`${value}T12:00:00`));
    }

    function shortDateParts(value) {
        if (!value) return { day: "—", month: "TBD" };
        const date = new Date(`${value}T12:00:00`);
        return {
            day: new Intl.DateTimeFormat(undefined, { day: "2-digit" }).format(date),
            month: new Intl.DateTimeFormat(undefined, { month: "short" }).format(date)
        };
    }

    function setConnection(connected) {
        const label = byId("connection-label");
        const dot = document.querySelector(".connection-dot");
        label.textContent = connected ? "API connected" : "API unavailable";
        dot.classList.toggle("offline", !connected);
    }

    function setSummary(summary) {
        state.summary = summary;
        byId("stat-events").textContent = summary.totalEvents;
        byId("stat-volunteers").textContent = summary.totalVolunteers;
        byId("stat-signups").textContent = summary.totalSignups;
        byId("stat-attendance").textContent = summary.totalAttendanceRecords;
        byId("stat-hours").textContent = Number(summary.totalVolunteerHours).toLocaleString();
        byId("impact-hours").textContent = Number(summary.totalVolunteerHours).toLocaleString();
        byId("impact-volunteers").textContent = summary.totalVolunteers;
    }

    function renderOverviewEvents(events) {
        const list = byId("overview-events");
        if (!events.length) {
            list.innerHTML = '<p class="empty-note">No upcoming events yet. Create one to get started.</p>';
            return;
        }
        list.innerHTML = events.slice(0, 4).map((event) => {
            const date = shortDateParts(event.date);
            return `<article class="event-item">
                <div class="event-date"><b>${escapeHtml(date.day)}</b><span>${escapeHtml(date.month)}</span></div>
                <div><h3>${escapeHtml(event.name)}</h3><p>${escapeHtml(event.location)} · ${escapeHtml(formatDate(event.date))}</p></div>
                <span class="capacity-note">${escapeHtml(event.volunteerCapacity)} spots</span>
            </article>`;
        }).join("");
    }

    function renderEventRows(events) {
        const body = byId("events-table-body");
        byId("events-empty").hidden = events.length > 0;
        if (!events.length) {
            body.innerHTML = "";
            return;
        }
        body.innerHTML = events.map((event) => `<tr>
            <td><div class="event-cell"><span class="event-mini-mark" aria-hidden="true">✳</span><strong>${escapeHtml(event.name)}</strong></div></td>
            <td>${escapeHtml(formatDate(event.date))}</td>
            <td>${escapeHtml(event.location)}</td>
            <td>${escapeHtml(event.volunteerCapacity)} volunteers</td>
            <td><button class="table-action" type="button" data-signup-event="${event.id}">Sign up</button></td>
        </tr>`).join("");
    }

    function renderVolunteerEvents(events) {
        const host = byId("volunteer-events");
        byId("volunteer-event-count").textContent = `${events.length} ${events.length === 1 ? "event" : "events"}`;
        if (!events.length) {
            host.innerHTML = '<p class="empty-note">There are no upcoming events right now. Please check back soon.</p>';
            return;
        }
        host.innerHTML = events.map((event) => `<article class="volunteer-event-card">
            <div class="volunteer-event-date"><span>${escapeHtml(formatDate(event.date))}</span><span>${escapeHtml(event.location)}</span></div>
            <h3>${escapeHtml(event.name)}</h3>
            <p>${escapeHtml(event.description || "Join your neighbors and make a difference.")}</p>
            <div class="volunteer-event-footer"><span>${escapeHtml(event.volunteerCapacity)} volunteer spots</span><button class="primary-button" type="button" data-volunteer-register="${event.id}" data-event-name="${escapeHtml(event.name)}">Register</button></div>
        </article>`).join("");
    }

    function renderVolunteerRows(volunteers) {
        const body = byId("volunteers-table-body");
        byId("volunteer-count").textContent = `${volunteers.length} ${volunteers.length === 1 ? "person" : "people"}`;
        byId("volunteers-empty").hidden = volunteers.length > 0;
        if (!volunteers.length) {
            body.innerHTML = "";
            return;
        }
        body.innerHTML = [...volunteers].sort((left, right) => right.id - left.id).map((volunteer) => `<tr>
            <td><div class="volunteer-name-cell"><span class="inline-avatar">${escapeHtml(initials(volunteer.name))}</span><strong>${escapeHtml(volunteer.name)}</strong></div></td>
            <td>${escapeHtml(volunteer.email)}</td>
            <td>${escapeHtml(volunteer.phone || "—")}</td>
            <td><button class="table-action" type="button" data-volunteer-detail="${volunteer.id}">View impact</button></td>
            <td></td>
        </tr>`).join("");
    }

    function renderVolunteerPreview(volunteers) {
        const strip = byId("overview-volunteers");
        if (!volunteers.length) {
            strip.innerHTML = '<p class="empty-note">Your volunteer community will show here.</p>';
            return;
        }
        strip.innerHTML = volunteers.slice(0, 3).map((volunteer) => `<button class="volunteer-chip detail-chip" type="button" data-volunteer-detail="${volunteer.id}">
            <span class="avatar">${escapeHtml(initials(volunteer.name))}</span>
            <span><strong>${escapeHtml(volunteer.name)}</strong><small>${escapeHtml(volunteer.email)}</small></span>
        </button>`).join("");
    }

    function fillSelect(select, items, labelFor, placeholder) {
        const current = select.value;
        select.innerHTML = `<option value="">${escapeHtml(placeholder)}</option>` + items.map((item) =>
            `<option value="${item.id}">${escapeHtml(labelFor(item))}</option>`).join("");
        if (items.some((item) => String(item.id) === current)) select.value = current;
    }

    function refreshSelects() {
        fillSelect(byId("signup-event"), state.events, (event) => `${event.name} · ${formatDate(event.date)}`, "Choose an event");
        fillSelect(byId("signup-volunteer"), state.volunteers, (volunteer) => volunteer.name, "Choose a volunteer");
        fillSelect(byId("roster-event"), state.events, (event) => event.name, "Choose event to view roster");
        fillSelect(byId("attendance-event"), state.events, (event) => event.name, "Choose event to view attendance");
    }

    async function loadData() {
        const refresh = byId("refresh-button");
        refresh.disabled = true;
        refresh.classList.add("is-loading");
        try {
            const [summary, events, upcoming, volunteers] = await Promise.all([
                request("/api/dashboard/summary"),
                request("/api/events"),
                request("/api/events/upcoming"),
                request("/api/volunteers")
            ]);
            state.events = events;
            state.upcoming = upcoming;
            state.volunteers = volunteers;
            setSummary(summary);
            renderOverviewEvents(upcoming);
            renderEventRows(events);
            renderVolunteerEvents(upcoming);
            renderVolunteerRows(volunteers);
            renderVolunteerPreview(volunteers);
            refreshSelects();
            setConnection(true);
        } catch (error) {
            setConnection(false);
            showNotice(error.message || "Could not load VolunteerHub data.", true);
        } finally {
            refresh.disabled = false;
            refresh.classList.remove("is-loading");
        }
    }

    function switchView(name) {
        document.querySelectorAll(".view").forEach((view) => {
            const active = view.id === `view-${name}`;
            view.classList.toggle("active", active);
            view.hidden = !active;
        });
        document.querySelectorAll("[data-view-target]").forEach((button) => {
            button.classList.toggle("active", button.dataset.viewTarget === name && button.classList.contains("nav-item"));
        });
        const current = name.toUpperCase();
        byId("breadcrumb-current").textContent = current;
        document.title = `${current.charAt(0)}${current.slice(1).toLowerCase()} | VolunteerHub`;
    }

    async function searchEvents(location) {
        try {
            const query = location ? `?location=${encodeURIComponent(location)}` : "";
            renderEventRows(await request(`/api/events${query}`));
        } catch (error) {
            showNotice(error.message, true);
        }
    }

    async function loadRoster(eventId) {
        const list = byId("signup-list");
        if (!eventId) {
            list.innerHTML = '<p class="empty-note">Choose an event to view its signups.</p>';
            return;
        }
        try {
            const signups = await request(`/api/events/${eventId}/signups`);
            list.innerHTML = signups.length ? signups.map((signup) => {
                const volunteer = state.volunteers.find((item) => item.id === signup.volunteerId);
                return `<div class="roster-row">
                    <strong>${escapeHtml(volunteer?.name || `Volunteer #${signup.volunteerId}`)}</strong>
                    <span>Signed up ${escapeHtml(formatDate(signup.signupDate))}</span>
                    <button class="table-action" type="button" data-mark-signup="${signup.id}" data-event-id="${eventId}">Record attendance</button>
                </div>`;
            }).join("") : '<p class="empty-note">No registrations for this event yet.</p>';
        } catch (error) {
            list.innerHTML = `<p class="empty-note">${escapeHtml(error.message)}</p>`;
        }
    }

    async function loadAttendance(eventId) {
        const list = byId("attendance-list");
        const signupSelect = byId("attendance-signup");
        if (!eventId) {
            list.innerHTML = '<p class="empty-note">Choose an event to view attendance.</p>';
            signupSelect.innerHTML = '<option value="">Choose a signup</option>';
            return;
        }
        try {
            const [records, signups] = await Promise.all([
                request(`/api/events/${eventId}/attendance`),
                request(`/api/events/${eventId}/signups`)
            ]);
            signupSelect.innerHTML = '<option value="">Choose a signup</option>' + signups.map((signup) => {
                const volunteer = state.volunteers.find((item) => item.id === signup.volunteerId);
                return `<option value="${signup.id}">#${signup.id} · ${escapeHtml(volunteer?.name || `Volunteer #${signup.volunteerId}`)}</option>`;
            }).join("");
            list.innerHTML = records.length ? records.map((record) => {
                const signup = signups.find((item) => item.id === record.signupId);
                const volunteer = state.volunteers.find((item) => item.id === signup?.volunteerId);
                return `<div class="roster-row">
                    <strong>${escapeHtml(volunteer?.name || `Signup #${record.signupId}`)}</strong>
                    <span>${record.attended ? "Present" : "Absent"} · ${escapeHtml(record.hoursContributed)} hrs</span>
                    <button class="table-action" type="button" data-edit-attendance="${record.signupId}" data-present="${record.attended}" data-hours="${record.hoursContributed}">Edit</button>
                </div>`;
            }).join("") : '<p class="empty-note">No attendance has been recorded yet.</p>';
        } catch (error) {
            list.innerHTML = `<p class="empty-note">${escapeHtml(error.message)}</p>`;
        }
    }

    async function showVolunteerImpact(volunteerId) {
        const volunteer = state.volunteers.find((item) => item.id === Number(volunteerId));
        if (!volunteer) return;
        byId("history-title").textContent = volunteer.name;
        byId("history-content").innerHTML = '<p class="loading-line">Loading volunteer impact…</p>';
        byId("history-dialog").showModal();
        try {
            const [hours, history] = await Promise.all([
                request(`/api/volunteers/${volunteerId}/hours`),
                request(`/api/volunteers/${volunteerId}/history`)
            ]);
            byId("history-content").innerHTML = `<div class="history-total"><strong>${escapeHtml(hours.totalHours)}</strong><span>total volunteer hours</span></div>` +
                (history.length ? history.map((item) => `<div class="history-row">
                    <span><strong>${escapeHtml(item.eventName)}</strong><small>${escapeHtml(formatDate(item.date))} · ${escapeHtml(item.location || "Location not listed")} · ${item.attended ? "Present" : "Absent"}</small></span>
                    <span class="history-hours">${escapeHtml(item.hours)} hrs</span>
                </div>`).join("") : '<p class="empty-note">No participation history yet.</p>');
        } catch (error) {
            byId("history-content").innerHTML = `<p class="empty-note">${escapeHtml(error.message)}</p>`;
        }
    }

    function openDialog(id) {
        const dialog = byId(id);
        if (dialog && !dialog.open) dialog.showModal();
    }

    function closeDialogs() {
        document.querySelectorAll("dialog[open]").forEach((dialog) => dialog.close());
    }

    document.addEventListener("click", (event) => {
        const viewButton = event.target.closest("[data-view-target]");
        if (viewButton) switchView(viewButton.dataset.viewTarget);

        const openButton = event.target.closest("[data-open-dialog]");
        if (openButton) openDialog(openButton.dataset.openDialog);

        if (event.target.closest("[data-close-dialog]")) closeDialogs();

        const publicSignupButton = event.target.closest("[data-volunteer-register]");
        if (publicSignupButton) {
            byId("public-signup-event-id").value = publicSignupButton.dataset.volunteerRegister;
            byId("public-signup-title").textContent = `Register for ${publicSignupButton.dataset.eventName}`;
            openDialog("public-signup-dialog");
        }

        const volunteerButton = event.target.closest("[data-volunteer-detail]");
        if (volunteerButton) showVolunteerImpact(volunteerButton.dataset.volunteerDetail);

        const signupEventButton = event.target.closest("[data-signup-event]");
        if (signupEventButton) {
            switchView("signups");
            byId("signup-event").value = signupEventButton.dataset.signupEvent;
            byId("signup-event").focus();
        }

        const signupButton = event.target.closest("[data-mark-signup]");
        if (signupButton) {
            switchView("attendance");
            byId("attendance-event").value = signupButton.dataset.eventId;
            loadAttendance(signupButton.dataset.eventId).then(() => { byId("attendance-signup").value = signupButton.dataset.markSignup; });
        }

        const editAttendance = event.target.closest("[data-edit-attendance]");
        if (editAttendance) {
            byId("attendance-signup").value = editAttendance.dataset.editAttendance;
            byId("attendance-present").checked = editAttendance.dataset.present === "true";
            byId("attendance-hours").value = editAttendance.dataset.hours;
            switchView("attendance");
            byId("attendance-signup").focus();
        }
    });

    document.querySelectorAll("dialog").forEach((dialog) => {
        dialog.addEventListener("click", (event) => {
            if (event.target === dialog) dialog.close();
        });
    });

    byId("refresh-button").addEventListener("click", loadData);
    byId("event-search-form").addEventListener("submit", (event) => {
        event.preventDefault();
        searchEvents(byId("event-location-search").value.trim());
    });
    byId("clear-search").addEventListener("click", () => {
        byId("event-location-search").value = "";
        searchEvents("");
    });
    byId("roster-event").addEventListener("change", (event) => loadRoster(event.target.value));
    byId("attendance-event").addEventListener("change", (event) => loadAttendance(event.target.value));
    byId("attendance-present").addEventListener("change", (event) => {
        if (!event.target.checked) byId("attendance-hours").value = "0";
        byId("attendance-hours").disabled = !event.target.checked;
    });

    byId("event-form").addEventListener("submit", async (event) => {
        event.preventDefault();
        const form = new FormData(event.currentTarget);
        const payload = {
            name: form.get("name").trim(),
            description: form.get("description").trim(),
            date: form.get("date"),
            location: form.get("location").trim(),
            volunteerCapacity: Number(form.get("volunteerCapacity"))
        };
        try {
            await request("/api/events", { method: "POST", body: JSON.stringify(payload) });
            event.currentTarget.reset();
            byId("event-capacity").value = "20";
            closeDialogs();
            await loadData();
            switchView("events");
            showNotice("Event created and added to the calendar.");
        } catch (error) {
            showNotice(error.message, true);
        }
    });

    byId("volunteer-form").addEventListener("submit", async (event) => {
        event.preventDefault();
        const form = new FormData(event.currentTarget);
        const payload = {
            name: form.get("name").trim(),
            email: form.get("email").trim(),
            phone: form.get("phone").trim() || null
        };
        try {
            await request("/api/volunteers", { method: "POST", body: JSON.stringify(payload) });
            event.currentTarget.reset();
            closeDialogs();
            await loadData();
            switchView("volunteers");
            showNotice("Volunteer added to the community.");
        } catch (error) {
            showNotice(error.message, true);
        }
    });

    byId("public-signup-form").addEventListener("submit", async (event) => {
        event.preventDefault();
        const form = new FormData(event.currentTarget);
        const email = String(form.get("email")).trim().toLowerCase();
        const name = String(form.get("name")).trim();
        const eventId = Number(form.get("eventId"));
        const submit = byId("public-signup-submit");
        submit.disabled = true;
        let volunteer;
        try {
            volunteer = state.volunteers.find((item) => item.email.toLowerCase() === email);
            if (volunteer && volunteer.name.toLowerCase() !== name.toLowerCase()) {
                throw new Error("That email belongs to a different name. Please check your details.");
            }
            if (!volunteer) {
                volunteer = await request("/api/volunteers", {
                    method: "POST",
                    body: JSON.stringify({ name, email, phone: String(form.get("phone")).trim() || null })
                });
            }
            await request("/api/signups", {
                method: "POST",
                body: JSON.stringify({ eventId, volunteerId: volunteer.id })
            });
            event.currentTarget.reset();
            closeDialogs();
            await loadData();
            switchView("volunteer-portal");
            showNotice(`You’re registered for this event, ${volunteer.name}. Thanks for volunteering!`);
        } catch (error) {
            if (volunteer && state.volunteers.every((item) => item.id !== volunteer.id)) await loadData();
            showNotice(error.message, true);
        } finally {
            submit.disabled = false;
        }
    });

    byId("signup-form").addEventListener("submit", async (event) => {
        event.preventDefault();
        const eventId = Number(byId("signup-event").value);
        const selectedVolunteerId = byId("signup-volunteer").value;
        const name = byId("signup-name").value.trim();
        const email = byId("signup-email").value.trim();
        if (!selectedVolunteerId && (!name || !email)) {
            showNotice("Choose an existing volunteer or enter a name and email.", true);
            return;
        }
        try {
            let volunteerId = Number(selectedVolunteerId);
            if (!selectedVolunteerId) {
                const volunteer = await request("/api/volunteers", {
                    method: "POST",
                    body: JSON.stringify({ name, email, phone: byId("signup-phone").value.trim() || null })
                });
                volunteerId = volunteer.id;
            }
            await request("/api/signups", { method: "POST", body: JSON.stringify({ eventId, volunteerId }) });
            byId("signup-form").reset();
            byId("roster-event").value = String(eventId);
            await loadRoster(eventId);
            await loadData();
            showNotice("Volunteer registered for the event.");
        } catch (error) {
            showNotice(error.message, true);
        }
    });

    byId("attendance-form").addEventListener("submit", async (event) => {
        event.preventDefault();
        const signupId = byId("attendance-signup").value;
        const payload = {
            attended: byId("attendance-present").checked,
            hoursContributed: Number(byId("attendance-hours").value)
        };
        try {
            await request(`/api/attendance/${signupId}`, { method: "PUT", body: JSON.stringify(payload) });
            await loadAttendance(byId("attendance-event").value);
            await loadData();
            showNotice("Attendance saved.");
        } catch (error) {
            showNotice(error.message, true);
        }
    });

    const now = new Date();
    byId("today-label").textContent = new Intl.DateTimeFormat(undefined, { weekday: "short", month: "short", day: "numeric" }).format(now);
    byId("welcome-date").textContent = new Intl.DateTimeFormat(undefined, { weekday: "long", month: "long", day: "numeric" }).format(now).toUpperCase();
    if (window.location.hash === "#volunteer-portal") switchView("volunteer-portal");
    loadData();
})();
