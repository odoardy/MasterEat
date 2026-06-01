document.documentElement.classList.remove("no-js");
document.documentElement.classList.add("js");

document.addEventListener("DOMContentLoaded", () => {
    // Riferimenti agli elementi interattivi globali della pagina.
    const header = document.querySelector(".site-header");
    const toggle = document.querySelector(".nav-toggle");
    const navigation = document.querySelector("#site-navigation");
    const backToTop = document.querySelector(".back-to-top");

    // Apre e chiude la navigazione mobile, mantenendo aggiornato lo stato ARIA.
    if (toggle && navigation) {
        toggle.addEventListener("click", () => {
            const isOpen = navigation.classList.toggle("is-open");
            toggle.setAttribute("aria-expanded", String(isOpen));
            showHeader();
        });
    }

    // Intercetta i form con data-confirm e mostra una conferma prima del submit.
    document.querySelectorAll("form[data-confirm]").forEach((form) => {
        form.addEventListener("submit", (event) => {
            const message = form.getAttribute("data-confirm");
            if (message && !window.confirm(message)) {
                event.preventDefault();
            }
        });
    });

    function showHeader() {
        if (header) {
            header.classList.remove("site-header--hidden");
        }
    }

    function hideHeader() {
        if (header) {
            header.classList.add("site-header--hidden");
        }
    }

    function setHeaderScrolled(isScrolled) {
        if (header) {
            header.classList.toggle("is-scrolled", isScrolled);
        }
    }

    function isHeaderLocked() {
        // L'header resta visibile mentre menu o dropdown account sono aperti/interagiti.
        return Boolean(
            (header && header.querySelector(".account-dropdown[open], .account-dropdown:focus-within, .account-dropdown:hover"))
            || (navigation && navigation.classList.contains("is-open"))
        );
    }

    function setBackToTopVisible(isVisible) {
        if (!backToTop) {
            return;
        }

        if (isVisible) {
            backToTop.hidden = false;
            backToTop.classList.add("is-visible");
        } else {
            backToTop.classList.remove("is-visible");
            backToTop.hidden = true;
        }
    }

    if (backToTop) {
        // Il bottone torna all'inizio pagina e forza la riapparizione dell'header.
        backToTop.addEventListener("click", () => {
            showHeader();
            window.scrollTo({
                top: 0,
                behavior: "smooth"
            });
        });
    }

    let lastScrollY = Math.max(window.scrollY, 0);
    let ticking = false;
    const headerThreshold = 96;
    const directionDelta = 8;

    function getBackToTopThreshold() {
        const scrollHeight = document.documentElement.scrollHeight;
        const viewportHeight = window.innerHeight;
        const scrollableHeight = Math.max(scrollHeight - viewportHeight, 0);
        return scrollableHeight / 2;
    }

    // Aggiorna header e back-to-top in base a posizione e direzione dello scroll.
    function handleScroll() {
        const currentScrollY = Math.max(window.scrollY, 0);
        setHeaderScrolled(currentScrollY > 4);

        if (currentScrollY <= headerThreshold || isHeaderLocked()) {
            showHeader();
        } else if (currentScrollY > lastScrollY + directionDelta) {
            hideHeader();
        } else if (currentScrollY < lastScrollY - directionDelta) {
            showHeader();
        }

        setBackToTopVisible(currentScrollY > getBackToTopThreshold());

        if (Math.abs(currentScrollY - lastScrollY) > directionDelta || currentScrollY <= headerThreshold) {
            lastScrollY = currentScrollY;
        }

        ticking = false;
    }

    window.addEventListener("scroll", () => {
        if (!ticking) {
            // requestAnimationFrame evita di aggiornare il DOM troppe volte durante lo scroll.
            window.requestAnimationFrame(handleScroll);
            ticking = true;
        }
    }, { passive: true });

    handleScroll();
});
