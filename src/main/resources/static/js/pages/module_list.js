$(document).ready(function () {
    $('.table').DataTable({
        "columnDefs": [{
            "targets": 0,
            "orderable": false
        }, {"targets": 1}, {"targets": 2}, {"targets": 3}, {"targets": 4}]
    });
});

function showConfirmationModal(button) {
    const dataOrgMaskedId = button.getAttribute('data-org-uid');
    const dataModMaskedId = button.getAttribute('data-mod-uid');
    const deleteUrl = '/admin/module/delete?ouid=' + dataOrgMaskedId + '&uid=' + dataModMaskedId;
    document.getElementById('deleteLink').href = deleteUrl;
    $('#confirmationModal').modal('show');
}
