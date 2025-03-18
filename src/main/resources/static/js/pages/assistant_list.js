$(document).ready(function () {
    $('.table').DataTable({
        "columnDefs": [{"targets": 0, "orderable": false}, {"targets": 1}, {"targets": 2}, {
            "targets": 3,
            "orderable": false
        }, {"targets": 4}, {"targets": 5}]
    });
});
const tooltipTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="tooltip"]'));
const tooltipList = tooltipTriggerList.map(function (tooltipTriggerEl) {
    return new bootstrap.Tooltip(tooltipTriggerEl)
});

function showConfirmationModal(button) {
    const name = button.getAttribute('field-data');
    document.getElementById('deleteLink').href = '/admin/assistants/delete?uid=' + name;
    $('#confirmationModal').modal('show');
}

function showDataDumpModal(button) {
    const name = button.getAttribute('field-data');
    const downloadLink = document.getElementById('dataDumpLink');
    downloadLink.href = '/admin/assistants/export?uid=' + name;
    $('#dataDumpModal').modal('show');
    downloadLink.addEventListener('click', function () {
        $('#dataDumpModal').modal('hide');
    });
}
