(() => {
    const $ = (id) => document.getElementById(id);

    const q = $("q");
    const secFields = $("secFields");
    const secConstructors = $("secConstructors");
    const secMethods = $("secMethods");

    const visPublic = $("visPublic");
    const visProtected = $("visProtected");
    const visPackage = $("visPackage");
    const visPrivate = $("visPrivate");

    const showSynth = $("showSynth");

    function selectedVis() {
        const set = new Set();
        if (visPublic.checked) set.add("public");
        if (visProtected.checked) set.add("protected");
        if (visPackage.checked) set.add("package");
        if (visPrivate.checked) set.add("private");
        return set;
    }

    function apply() {
        const needle = (q.value || "").trim().toLowerCase();
        const vis = selectedVis();

        document.querySelectorAll("section.cls").forEach(cls => {
            const clsName = (cls.dataset.name || "").toLowerCase();
            const classMatches = needle === "" || clsName.includes(needle);

            let anyVisibleInClass = false;

            cls.querySelectorAll("[data-kind]").forEach(m => {
                const kind = m.dataset.kind;
                const v = m.dataset.visibility;
                const synth = m.dataset.synth === "true";

                const allowKind = (kind === "field" && secFields.checked) || (kind === "ctor" && secConstructors.checked) || (kind === "method" && secMethods.checked);

                const allowVis = vis.has(v);
                const allowSynth = showSynth.checked || !synth;

                const memberText = (m.dataset.search || "").toLowerCase();
                const memberMatches = needle === "" || classMatches || memberText.includes(needle);

                const show = allowKind && allowVis && allowSynth && memberMatches;
                m.classList.toggle("hidden", !show);
                if (show) anyVisibleInClass = true;
            });

            cls.classList.toggle("hidden", !(classMatches || anyVisibleInClass));

            cls.querySelectorAll("details.block").forEach(block => {
                const any = Array.from(block.querySelectorAll("[data-kind]")).some(x => !x.classList.contains("hidden"));
                block.classList.toggle("hidden", !any);
            });
        });
    }

    [q, secFields, secConstructors, secMethods, visPublic, visProtected, visPackage, visPrivate, showSynth]
        .forEach(el => el.addEventListener("input", apply));

    apply();
})();