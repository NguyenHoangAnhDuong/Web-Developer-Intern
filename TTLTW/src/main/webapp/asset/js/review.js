document.addEventListener('DOMContentLoaded', function () {
    const stars = document.querySelectorAll('.review-modal .rating .star');
    const ratingInput = document.getElementById('ratingInput');
    const ratingError = document.getElementById('ratingError');
    const commentField = document.getElementById('comment');
    const charCount = document.getElementById('charCount');
    const reviewForm = document.getElementById('reviewForm');
    if (stars && ratingInput) {
        stars.forEach(function (star) {
            star.addEventListener('click', function () {
                const value = this.dataset.value;
                ratingInput.value = value;
                ratingError.classList.remove('visible');
                stars.forEach(function (item) {
                    item.classList.toggle('active', Number(item.dataset.value) <= Number(value));
                    const icon = item.querySelector('i');
                    if (item.classList.contains('active')) {
                        icon.classList.remove('fa-regular');
                        icon.classList.add('fa-solid');
                    } else {
                        icon.classList.remove('fa-solid');
                        icon.classList.add('fa-regular');
                    }
                });
            });
        });
    }
    if (commentField && charCount) {
        charCount.textContent = commentField.value.length;
        commentField.addEventListener('input', function () {
            charCount.textContent = this.value.length;
        });
    }
    if (reviewForm) {
        reviewForm.addEventListener('submit', function (event) {
            if (!ratingInput.value) {
                event.preventDefault();
                ratingError.classList.add('visible');
            }
        });
    }
});
