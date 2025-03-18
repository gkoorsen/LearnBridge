$(document).ready(function () {
    $('.table').DataTable({
        "columnDefs": [{
            "targets": 0,
            "orderable": false
        }, {"targets": 1}, {"targets": 2}, {"targets": 3}, {"targets": 4}, {"targets": 5}, {"targets": 6}]
    });
});
const tooltipTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="tooltip"]'));
const tooltipList = tooltipTriggerList.map(function (tooltipTriggerEl) {
    return new bootstrap.Tooltip(tooltipTriggerEl)
});

function showConfirmationModal(button) {
    const fieldValue = button.getAttribute('field-data');
    document.getElementById('deleteLink').href = '/admin/users/delete?uid=' + fieldValue;
    $('#confirmationModal').modal('show');
}
