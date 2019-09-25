$j(
$j(window).bind('scroll', function() {
         if ($j(window).scrollTop() > 193) {
             $j('#docuTableOfContents').addClass('fixed');
         }
         else {
             $j('#docuTableOfContents').removeClass('fixed');
         }
    })
)