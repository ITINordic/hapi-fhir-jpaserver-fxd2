output.setOptionalValue(args['uniqueIdAttribute'], output.getIdentifier());
output.setValue(args['lastNameAttribute'], humanNameUtils.getPrimaryName(input.name).family, context.getFhirRequest().getLastUpdated());
output.setValue(args['firstNameAttribute'], humanNameUtils.getSingleGiven(humanNameUtils.getPrimaryName(input.name)), context.getFhirRequest().getLastUpdated());
var birthDate = dateTimeUtils.getPreciseDate(input.birthDateElement);
if ((birthDate != null) || args['resetDhisValue']){  
output.setOptionalValue(args['birthDateAttribute'], birthDate, context.getFhirRequest().getLastUpdated());
}
if ((input.gender != null) || args['resetDhisValue']){  
output.setOptionalValue(args['genderAttribute'], input.gender, context.getFhirRequest().getLastUpdated());
}
var addressText = addressUtils.getConstructedText(addressUtils.getPrimaryAddress(input.address));
if ((addressText != null) || args['resetDhisValue']){  
output.setOptionalValue(args['addressTextAttribute'], addressText, context.getFhirRequest().getLastUpdated());
}
true
