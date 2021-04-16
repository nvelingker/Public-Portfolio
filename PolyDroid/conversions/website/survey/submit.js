// ajax submission for Google sheets
var $form = $('form#test-form'),
    url = 'https://script.google.com/macros/s/AKfycbwLG2JMoCrmAhys5pnwSwJXKK-352kRNUaLSOFRSgWxbfwUJlRJ/exec'

$('#submit-form').on('click', function(e) {
  e.preventDefault();
  var jqxhr = $.ajax({
    url: url,
    method: "GET",
    dataType: "json",
    data: $form.serializeObject()
  }).success(
    // do something
  );
})
