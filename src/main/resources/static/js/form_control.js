function disableButton() {
    const button = document.getElementById("form_main_submit_button");
    const spinner = document.getElementById("form_main_submit_spinner");
    const icon = document.getElementById("form_main_submit_icon");
    const form = document.getElementById("form_main_content");
    if (form.checkValidity()) {
        button.disabled = true;
        icon.style.display = "none";
        spinner.classList.remove("d-none");
        form.submit();
    } else {
        form.reportValidity();
    }
}