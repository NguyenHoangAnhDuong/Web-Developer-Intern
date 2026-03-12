const promoList = document.querySelector(".promo-slider .promo-list");
const promoNext = document.querySelector(".promo-control.next i");
const promoPrev = document.querySelector(".promo-control.prev i");

if (promoNext && promoPrev) {
    promoNext.addEventListener("click", () => {
        promoList.scrollBy({left: 200, behavior: "smooth"});
    });
    promoPrev.addEventListener("click", () => {
        promoList.scrollBy({left: -200, behavior: "smooth"});
    });
}