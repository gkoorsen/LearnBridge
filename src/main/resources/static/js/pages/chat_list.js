$(document).ready(function () {
    $('.table').DataTable({
        "columnDefs": [{"targets": 0, "orderable": false}, {"targets": 1}, {"targets": 2}, {"targets": 3},]
    });
});
const tooltipTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="tooltip"]'));
const tooltipList = tooltipTriggerList.map(function (tooltipTriggerEl) {
    return new bootstrap.Tooltip(tooltipTriggerEl)
});

function showConfirmationModal(button) {
    const fieldValue = button.getAttribute('field-data');
    document.getElementById('deleteLink').href = '/chat/delete?uid=' + fieldValue;
    $('#confirmationModal').modal('show');
}
