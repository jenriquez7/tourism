package com.tourism.util;

public final class MessageConstants {

    private MessageConstants(){}

    public static final String GENERIC_ERROR = "Something goes wrong";
    public static final String NULL_ID = "null id";
    public static final String NULL_NAME = "null name";
    public static final String NULL_EMAIL = "null email";
    public static final String NULL_LAST_NAME = "null last name";
    public static final String ERROR_EMPTY_EMAIL = "Email is null or empty";
    public static final String ERROR_INVALID_EMAIL_FORMAT = "Invalid email format";
    public static final String ERROR_REQUIRED_PASSWORD = "Password is required";
    public static final String ERROR_PASSWORD_LENGTH = "Password must be between 8 and 30 characters long";
    public static final String ERROR_PASSWORD_UPPERCASE = "Password must contain at least one uppercase letter";
    public static final String ERROR_PASSWORD_LOWERCASE = "Password must contain at least one lowercase letter";
    public static final String ERROR_PASSWORD_NUMBER = "Password must contain at least one number";
    public static final String ERROR_PASSWORD_SPECIAL_CHARACTER = "Password must contain at least one special character";
    public static final String ERROR_INCORRECT_USER_OR_PASSWORD = "Incorrect user or password";
    public static final String ERROR_INVALID_TOKEN = "Invalid token";
    public static final String ERROR_REFRESH_TOKEN_NOT_FOUND = "Refresh token not found";
    public static final String ERROR_PRICING_METHOD_NOT_FOUND = "Estrategia de cálculo no encontrada: ";

    public static final String ERROR_DELETING_TOURISTIC_PLACE = "Error to delete touristic place";
    public static final String ERROR_GET_TOURISTIC_PLACE = "Error to get touristic place";
    public static final String ERROR_CREATE_TOURISTIC_PLACE = "Error to create touristic place";
    public static final String ERROR_UPDATE_TOURISTIC_PLACE = "Error to update touristic place";

    public static final String ERROR_CATEGORY_NOT_FOUND = "category not found";
    public static final String ERROR_CATEGORY_NOT_CREATED = "Category not created";
    public static final String ERROR_CATEGORY_NOT_UPDATED = "Category not updated";
    public static final String ERROR_GET_CATEGORY = "Error to get a Category";
    public static final String ERROR_DELETING_CATEGORY = "Error to delete category";

    public static final String ERROR_ADMIN_NOT_CREATED = "Admin not created";
    public static final String ERROR_CREATE_ADMIN = "Error to create an admin";
    public static final String ERROR_GET_ADMINS = "Error to get admins";
    public static final String ERROR_CANNOT_DELETE_LAST_ADMIN = "cannot delete last admin";
    public static final String ERROR_ADMIN_NOT_FOUND = "admin not found";

    public static final String ERROR_BOOKING_NOT_CREATED = "Booking not created";
    public static final String ERROR_BOOKING_NOT_FOUND = "Booking not found";
    public static final String ERROR_BOOKING_NOT_UPDATED = "Booking not updated";
    public static final String ERROR_DELETING_BOOKING = "Error to delete booking";
    public static final String ERROR_GET_BOOKING = "Error to get a booking";
    public static final String ERROR_INVALID_BOOKING_CHANGE_STATE = "Invalid change state";
    public static final String ERROR_BOOKING_CHANGE_STATE = "Error to booking change state";
    public static final String ERROR_USER_LODGING_OWNER = "User is not the lodging owner";

    public static final String ERROR_LODGING_OWNER_NOT_CREATED = "Lodging owner not created";
    public static final String ERROR_GET_LODGING_OWNER = "Error to get a lodging owner";
    public static final String ERROR_DELETING_LODGING_OWNER = "Error to delete lodging owner";

    public static final String ERROR_TOURIST_NOT_CREATED = "Tourist not created";
    public static final String ERROR_GET_TOURISTS = "Error to get list of tourists";
    public static final String ERROR_DELETING_TOURIST = "Error to delete tourist";
    public static final String ERROR_GET_TOURIST = "Error to get a tourist";
    public static final String ERROR_TOURIST_NOT_FOUND = "Tourist not found";
    public static final String ERROR_USER_TOURIST = "The user is not the booking tourist";

    public static final String ERROR_LODGING_NOT_CREATED = "Lodging not created";
    public static final String ERROR_FULL_CAPACITY = "Invalid update. lodging has more bookings than new capacity";
    public static final String ERROR_LODGING_LODGING_OWNER = "Invalid update. lodging do not belong to owner";
    public static final String ERROR_LODGING_NOT_UPDATED = "Lodging not updated";
    public static final String ERROR_GET_LODGINGS = "Error to get list of lodging";
    public static final String ERROR_DELETING_LODGING = "Error to delete lodging";
    public static final String ERROR_LODGING_NOT_FOUND = "Lodging not found";

    public static final String ERROR_BOOKING_DATES = "There are some problem with check in and check out dates";
    public static final String ERROR_CHECK_IN_AFTER_CHECKOUT = "checkIn date is after than checkOut date";
    public static final String ERROR_CHECK_IN_IN_THE_PAST = "checkIn date is in the past. Must be in present or future";
    public static final String ERROR_ENOUGH_CAPACITY = "Lodging hasn't enough capacity to this booking";
    public static final String ERROR_BOOKING_WITHOUT_ADULT = "At least one adult is necessary";

    public static final String ERROR_CREATE_NOTIFICATION = "Error al crear notificacion";
    public static final String CREATED_BOOKING_TOURIST = "Nuevo booking creado que requiere su atención: ";
    public static final String PENDING_BOOKING_TOURIST = "Se ha generado una reserva pendiente de confirmación y pago para ";
    public static final String BOOKING_ID_IS = "El id de la reserva es: ";
    public static final String REJECTED_BOOKING_TOURIST = " ha sido rechazada";
    public static final String YOUR_BOOKING_IN = "Su reserva en ";
    public static final String THE_BOOKING_IN = "La reserva en ";
    public static final String ACCEPTED_BOOKING_TOURIST = "Se ha confirmado su reserva en: ";
    public static final String EXPIRED_BOOKING = " se ha expirado";
    public static final String CREATED_BOOKING_OWNER = "Nuevo booking creado que requiere su atención: ";
    public static final String PENDING_BOOKING_OWNER = "Se ha aceptado correctamente la reserva para ";
    public static final String REJECTED_BOOKING_OWNER = "Ha quedado rechazado la reserva en: ";
    public static final String ACCEPTED_BOOKING_OWNER = "Se ha confirmado la reserva en: ";

}
