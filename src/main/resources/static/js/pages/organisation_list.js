$(document).ready(function () {
    $('.table').DataTable({
        "columnDefs": [{"targets": 0, "orderable": false}, {"targets": 1}, {"targets": 2, "orderable": false,}]
    });
});
const tooltipTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="tooltip"]'));
const tooltipList = tooltipTriggerList.map(function (tooltipTriggerEl) {
    return new bootstrap.Tooltip(tooltipTriggerEl)
});

function showConfirmationModal(button) {
    const fieldValue = button.getAttribute('field-data');
    document.getElementById('deleteLink').href = '/admin/organisation/delete?uid=' + fieldValue;
    $('#confirmationModal').modal('show');
}
