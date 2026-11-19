(function () {
  var token = document.querySelector('meta[name=_csrf]').content;
  var header = document.querySelector('meta[name=_csrf_header]').content;
  var dragId = null;

  document.querySelectorAll('.card').forEach(function (card) {
    card.addEventListener('dragstart', function (e) {
      dragId = card.dataset.id;
      card.classList.add('dragging');
      e.dataTransfer.setData('text/plain', dragId);
    });
    card.addEventListener('dragend', function () { card.classList.remove('dragging'); });
    card.addEventListener('click', function (e) {
      if (e.target.closest('a,button,form')) return;
      location.href = '/leads/' + card.dataset.id;
    });
  });

  document.querySelectorAll('.col').forEach(function (col) {
    col.addEventListener('dragover', function (e) { e.preventDefault(); col.classList.add('over'); });
    col.addEventListener('dragleave', function () { col.classList.remove('over'); });
    col.addEventListener('drop', function (e) {
      e.preventDefault();
      col.classList.remove('over');
      if (!dragId) return;
      var headers = { 'Content-Type': 'application/x-www-form-urlencoded' };
      headers[header] = token;
      fetch('/leads/' + dragId + '/mover', { method: 'POST', headers: headers, body: 'etapa=' + col.dataset.etapa })
        .then(function (r) {
          if (!r.ok) throw new Error('falha');
          if (col.dataset.etapa === 'PERDIDO') location.href = '/leads/' + dragId; // pede o motivo da perda
          else location.reload();
        })
        .catch(function () { alert('Não foi possível mover o lead. Atualize a página e tente de novo.'); });
    });
  });
})();
