function removeHighlighting() {
  $('span[data-hl]').each(function () {
    $(this).removeAttr('data-hl');
  });
}

function highlightSelection() {
  const classes = $(this).attr('class');
  if (classes == null) return;
  const classList = classes.split(/\s+/);
  const preLastClass = String(classList.slice(-2));
  const lastClass = String(classList.slice(-1));
  if (lastClass == null || preLastClass == null) {
    return;
  }
  if (lastClass.startsWith('r')) {
    if (preLastClass.startsWith('b')) {
      $('span.' + preLastClass).attr('data-hl', 'block');
    }
    $('span.' + lastClass).attr('data-hl', 'region');
  }
  else if (lastClass.startsWith('b')) {
    $('span.' + lastClass).attr('data-hl', 'block');
  }
  classList.forEach(function (cls) {
    if (cls.startsWith('d')) {
      const blockClass = cls.replace('d', 'b');
      $('span.' + blockClass).attr('data-hl', 'dependent');
    }
  });
}

window.addEventListener('load', function() {
  $('pre code span.r, pre code span.b')
    .hover(highlightSelection, removeHighlighting);
});