package org.alixar.daw2.JoseManuel.dwese_ticket_logger_webapp.controllers;

import jakarta.validation.Valid;
import org.alixar.daw2.JoseManuel.dwese_ticket_logger_webapp.entities.Almacen;
import org.alixar.daw2.JoseManuel.dwese_ticket_logger_webapp.entities.LicenciaAlmacen;
import org.alixar.daw2.JoseManuel.dwese_ticket_logger_webapp.repositories.AlmacenRepository;
import org.alixar.daw2.JoseManuel.dwese_ticket_logger_webapp.repositories.LicenciaAlmacenRepository;
import org.alixar.daw2.JoseManuel.dwese_ticket_logger_webapp.services.FileStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * Controlador encargado de gestionar las operaciones relacionadas
 * con las licencias de almacén.
 * Permite listar, crear, editar y eliminar licencias, además de
 * gestionar las validaciones, la subida de imágenes y los posibles errores.
 */
@Controller
@RequestMapping("/licencias")
public class LicenciaAlmacenController {

    private static final Logger logger =
            LoggerFactory.getLogger(LicenciaAlmacenController.class);

    @Autowired
    private LicenciaAlmacenRepository licenciaAlmacenRepository;

    @Autowired
    private AlmacenRepository almacenRepository;

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private MessageSource messageSource;

    /**
     * Muestra la lista de todas las licencias de almacén registradas.
     * Obtiene los datos de la base de datos y los envía a la vista.
     * Si se produce un error, muestra un mensaje de error.
     *
     * @param model modelo utilizado para enviar los datos a la vista
     * @param locale configuración regional para obtener los mensajes
     * @return nombre de la vista que muestra la lista de licencias
     */
    @GetMapping
    public String listLicencias(Model model, Locale locale) {
        logger.info("Solicitando la lista de licencias de almacén...");

        List<LicenciaAlmacen> listLicencias = null;

        try {
            listLicencias = licenciaAlmacenRepository.findAll();
            logger.info("Se han cargado {} licencias de almacén.", listLicencias.size());
        } catch (DataAccessException e) {
            logger.error("Error al listar las licencias de almacén: {}", e.getMessage());

            model.addAttribute("errorMessage",
                    messageSource.getMessage(
                            "msg.licencia-controller.list.error", null, locale));
        }

        model.addAttribute("listLicencias", listLicencias);

        return "licencias/licencia";
    }

    /**
     * Muestra el formulario para crear una nueva licencia de almacén.
     * Inicializa un objeto LicenciaAlmacen y un almacén vacío para
     * poder seleccionar uno en el formulario.
     *
     * @param model modelo utilizado para enviar los datos a la vista
     * @param redirectAttributes atributos utilizados para enviar mensajes
     *                           durante las redirecciones
     * @param locale configuración regional para obtener los mensajes
     * @return nombre de la vista del formulario de licencias
     */
    @GetMapping("/new")
    public String showNewForm(Model model, RedirectAttributes redirectAttributes,
                              Locale locale) {
        logger.info("Mostrando formulario para nueva licencia de almacén.");

        LicenciaAlmacen licencia = new LicenciaAlmacen();
        // Almacén vacío para que el formulario pueda enlazar almacen.id.
        licencia.setAlmacen(new Almacen());

        model.addAttribute("licencia", licencia);

        return loadAlmacenesAndShowForm(model, redirectAttributes, locale);
    }

    /**
     * Muestra el formulario para editar una licencia de almacén existente.
     * Busca la licencia mediante su identificador y comprueba
     * que exista antes de mostrar el formulario.
     *
     * @param id identificador de la licencia que se quiere editar
     * @param model modelo utilizado para enviar los datos a la vista
     * @param redirectAttributes atributos utilizados para enviar mensajes
     *                           durante las redirecciones
     * @param locale configuración regional para obtener los mensajes
     * @return nombre de la vista del formulario o redirección a la lista
     *         de licencias si se produce algún error
     */
    @GetMapping("/edit")
    public String showEditForm(@RequestParam("id") Long id, Model model,
                               RedirectAttributes redirectAttributes,
                               Locale locale) {
        logger.info("Mostrando formulario de edición para la licencia con ID {}", id);

        try {
            Optional<LicenciaAlmacen> licencia = licenciaAlmacenRepository.findById(id);

            if (licencia.isEmpty()) {
                logger.warn("No se encontró la licencia con ID {}", id);

                redirectAttributes.addFlashAttribute("errorMessage",
                        messageSource.getMessage(
                                "msg.licencia-controller.edit.notFound", null, locale));

                return "redirect:/licencias";
            }

            model.addAttribute("licencia", licencia.get());

        } catch (DataAccessException e) {
            logger.error("Error al obtener la licencia con ID {}: {}", id, e.getMessage());

            redirectAttributes.addFlashAttribute("errorMessage",
                    messageSource.getMessage(
                            "msg.licencia-controller.edit.error", null, locale));

            return "redirect:/licencias";
        }

        return loadAlmacenesAndShowForm(model, redirectAttributes, locale);
    }

    /**
     * Recibe los datos del formulario para insertar una nueva licencia de almacén.
     * Comprueba que los datos sean válidos, gestiona la subida del archivo de imagen
     * y verifica que el almacén elegido no tenga ya otra licencia antes de guardar
     * la licencia en la base de datos.
     *
     * @param licencia objeto con los datos de la licencia recibidos del formulario
     * @param result resultado de las validaciones del formulario
     * @param imageFile archivo de imagen subido en el formulario
     * @param model modelo utilizado para enviar los datos a la vista
     * @param redirectAttributes atributos utilizados para enviar mensajes
     *                           durante las redirecciones
     * @param locale configuración regional para obtener los mensajes
     * @return nombre de la vista del formulario si hay errores o redirección
     *         a la lista de licencias
     */
    @PostMapping("/insert")
    public String insertLicencia(@Valid @ModelAttribute("licencia") LicenciaAlmacen licencia,
                                 BindingResult result,
                                 @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
                                 Model model,
                                 RedirectAttributes redirectAttributes,
                                 Locale locale) {
        logger.info("Insertando nueva licencia de almacén con fecha {}", licencia.getDiaMatriculacion());

        try {
            checkAlmacenSelected(licencia, result);

            // Si se ha subido un archivo en el formulario, lo guardamos antes de comprobar imagen
            if (imageFile != null && !imageFile.isEmpty()) {
                String fileName = fileStorageService.saveFile(imageFile);
                if (fileName != null) {
                    licencia.setImagen(fileName);
                } else {
                    result.rejectValue("imagen", "msg.licencia-controller.image.error");
                }
            } else if (licencia.getImagen() == null || licencia.getImagen().isBlank()) {
                result.rejectValue("imagen", "msg.licencia.imagen.notBlank");
            }

            if (!result.hasFieldErrors("almacen.id")
                    && licenciaAlmacenRepository.existsByAlmacenId(licencia.getAlmacen().getId())) {
                logger.warn("El almacén {} ya tiene una licencia asignada.",
                        licencia.getAlmacen().getId());

                result.rejectValue("almacen.id",
                        "msg.licencia-controller.insert.almacenTaken");
            }

            if (result.hasErrors()) {
                return loadAlmacenesAndShowForm(model, redirectAttributes, locale);
            }

            licenciaAlmacenRepository.save(licencia);

            logger.info("Licencia de almacén insertada con éxito.");

        } catch (DataAccessException e) {
            logger.error("Error al insertar la licencia de almacén: {}", e.getMessage());

            redirectAttributes.addFlashAttribute("errorMessage",
                    messageSource.getMessage(
                            "msg.licencia-controller.insert.error", null, locale));
        }

        return "redirect:/licencias";
    }

    /**
     * Recibe los datos del formulario para actualizar una licencia de almacén existente.
     * Comprueba que los datos sean válidos, gestiona la actualización de la imagen si
     * se sube una nueva y verifica que el almacén elegido no tenga ya otra licencia
     * antes de guardar los cambios en la base de datos.
     *
     * @param licencia objeto con los datos actualizados de la licencia
     * @param result resultado de las validaciones del formulario
     * @param imageFile archivo de imagen subido en el formulario (opcional al editar)
     * @param model modelo utilizado para enviar los datos a la vista
     * @param redirectAttributes atributos utilizados para enviar mensajes
     *                           durante las redirecciones
     * @param locale configuración regional para obtener los mensajes
     * @return nombre de la vista del formulario si hay errores o redirección
     *         a la lista de licencias
     */
    @PostMapping("/update")
    public String updateLicencia(@Valid @ModelAttribute("licencia") LicenciaAlmacen licencia,
                                 BindingResult result,
                                 @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
                                 Model model,
                                 RedirectAttributes redirectAttributes,
                                 Locale locale) {
        logger.info("Actualizando licencia de almacén con ID {}", licencia.getId());

        try {
            checkAlmacenSelected(licencia, result);

            // Si se sube una nueva imagen al editar, reemplazamos la anterior
            if (imageFile != null && !imageFile.isEmpty()) {
                String oldImage = licencia.getImagen();
                String fileName = fileStorageService.saveFile(imageFile);
                if (fileName != null) {
                    licencia.setImagen(fileName);
                    if (oldImage != null && !oldImage.isBlank()) {
                        fileStorageService.deleteFile(oldImage);
                    }
                } else {
                    result.rejectValue("imagen", "msg.licencia-controller.image.error");
                }
            } else if (licencia.getImagen() == null || licencia.getImagen().isBlank()) {
                result.rejectValue("imagen", "msg.licencia.imagen.notBlank");
            }

            if (!result.hasFieldErrors("almacen.id")
                    && licenciaAlmacenRepository.existsByAlmacenIdAndIdNot(
                    licencia.getAlmacen().getId(), licencia.getId())) {
                logger.warn("El almacén {} ya tiene otra licencia asignada.",
                        licencia.getAlmacen().getId());

                result.rejectValue("almacen.id",
                        "msg.licencia-controller.update.almacenTaken");
            }

            if (result.hasErrors()) {
                return loadAlmacenesAndShowForm(model, redirectAttributes, locale);
            }

            licenciaAlmacenRepository.save(licencia);

            logger.info("Licencia de almacén con ID {} actualizada con éxito.", licencia.getId());

        } catch (DataAccessException e) {
            logger.error("Error al actualizar la licencia de almacén con ID {}: {}",
                    licencia.getId(), e.getMessage());

            redirectAttributes.addFlashAttribute("errorMessage",
                    messageSource.getMessage(
                            "msg.licencia-controller.update.error", null, locale));
        }

        return "redirect:/licencias";
    }

    /**
     * Elimina una licencia de almacén mediante su identificador
     * y borra su archivo de imagen asociado si existe.
     *
     * @param id identificador de la licencia que se quiere eliminar
     * @param redirectAttributes atributos utilizados para enviar mensajes
     *                           durante las redirecciones
     * @param locale configuración regional para obtener los mensajes
     * @return redirección a la lista de licencias
     */
    @PostMapping("/delete")
    public String deleteLicencia(@RequestParam("id") Long id,
                                 RedirectAttributes redirectAttributes,
                                 Locale locale) {
        logger.info("Eliminando licencia de almacén con ID {}", id);

        try {
            Optional<LicenciaAlmacen> licencia = licenciaAlmacenRepository.findById(id);

            licenciaAlmacenRepository.deleteById(id);

            if (licencia.isPresent() && licencia.get().getImagen() != null
                    && !licencia.get().getImagen().isBlank()) {
                fileStorageService.deleteFile(licencia.get().getImagen());
            }

            logger.info("Licencia de almacén con ID {} eliminada con éxito.", id);

        } catch (DataAccessException e) {
            logger.error("Error al eliminar la licencia de almacén con ID {}: {}", id, e.getMessage());

            redirectAttributes.addFlashAttribute("errorMessage",
                    messageSource.getMessage(
                            "msg.licencia-controller.delete.error", null, locale));
        }

        return "redirect:/licencias";
    }

    /**
     * Comprueba que se haya seleccionado un almacén en el desplegable.
     * Si no se ha seleccionado ninguno, añade un error de validación
     * al campo correspondiente.
     *
     * @param licencia licencia cuyos datos se quieren comprobar
     * @param result resultado donde se registra el error de validación
     */
    private void checkAlmacenSelected(LicenciaAlmacen licencia, BindingResult result) {
        if (licencia.getAlmacen() == null || licencia.getAlmacen().getId() == null) {
            result.rejectValue("almacen.id", "msg.licencia.almacen.notNull");
        }
    }

    /**
     * Carga la lista de almacenes y la añade al modelo para mostrar
     * el formulario de creación o edición de una licencia de almacén.
     * Si se produce un error al obtener los almacenes, realiza
     * una redirección a la lista de licencias mostrando un mensaje.
     *
     * @param model modelo utilizado para enviar los almacenes a la vista
     * @param redirectAttributes atributos utilizados para enviar mensajes
     *                           durante las redirecciones
     * @param locale configuración regional para obtener los mensajes
     * @return nombre de la vista del formulario o redirección a la lista
     *         de licencias si se produce un error
     */
    private String loadAlmacenesAndShowForm(Model model,
                                            RedirectAttributes redirectAttributes,
                                            Locale locale) {
        try {
            model.addAttribute("listAlmacenes", almacenRepository.findAll());

            return "licencias/licencia-form";

        } catch (DataAccessException e) {
            logger.error("Error al cargar los almacenes: {}", e.getMessage());

            redirectAttributes.addFlashAttribute("errorMessage",
                    messageSource.getMessage(
                            "msg.licencia-controller.almacenes.error", null, locale));

            return "redirect:/licencias";
        }
    }
}
