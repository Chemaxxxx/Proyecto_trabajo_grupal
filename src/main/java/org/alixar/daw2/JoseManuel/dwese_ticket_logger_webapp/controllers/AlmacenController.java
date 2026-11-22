package org.alixar.daw2.JoseManuel.dwese_ticket_logger_webapp.controllers;

import jakarta.validation.Valid;
import org.alixar.daw2.JoseManuel.dwese_ticket_logger_webapp.entities.Almacen;
import org.alixar.daw2.JoseManuel.dwese_ticket_logger_webapp.repositories.AlmacenRepository;
import org.alixar.daw2.JoseManuel.dwese_ticket_logger_webapp.repositories.LicenciaAlmacenRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * Controlador encargado de gestionar las operaciones relacionadas
 * con los almacenes.
 * Permite listar, crear, editar y eliminar almacenes, además de
 * gestionar las validaciones y los posibles errores.
 */
@Controller
@RequestMapping("/almacenes")
public class AlmacenController {

    private static final Logger logger =
            LoggerFactory.getLogger(AlmacenController.class);

    @Autowired
    private AlmacenRepository almacenRepository;

    @Autowired
    private LicenciaAlmacenRepository licenciaAlmacenRepository;

    @Autowired
    private MessageSource messageSource;

    /**
     * Muestra la lista de todos los almacenes registrados.
     * Obtiene los datos de la base de datos y los envía a la vista.
     * Si se produce un error, muestra un mensaje de error.
     *
     * @param model modelo utilizado para enviar los datos a la vista
     * @param locale configuración regional para obtener los mensajes
     * @return nombre de la vista que muestra la lista de almacenes
     */
    @GetMapping
    public String listAlmacenes(Model model, Locale locale) {
        logger.info("Solicitando la lista de almacenes...");

        List<Almacen> listAlmacenes = null;

        try {
            listAlmacenes = almacenRepository.findAll();
            logger.info("Se han cargado {} almacenes.", listAlmacenes.size());
        } catch (DataAccessException e) {
            logger.error("Error al listar los almacenes: {}", e.getMessage());

            model.addAttribute("errorMessage",
                    messageSource.getMessage(
                            "msg.almacen-controller.list.error", null, locale));
        }

        model.addAttribute("listAlmacenes", listAlmacenes);

        return "almacenes/almacen";
    }

    /**
     * Muestra el formulario para crear un nuevo almacén.
     * Inicializa un objeto Almacen vacío para el formulario.
     *
     * @param model modelo utilizado para enviar los datos a la vista
     * @return nombre de la vista del formulario de almacenes
     */
    @GetMapping("/new")
    public String showNewForm(Model model) {
        logger.info("Mostrando formulario para nuevo almacén.");

        model.addAttribute("almacen", new Almacen());

        return "almacenes/almacen-form";
    }

    /**
     * Muestra el formulario para editar un almacén existente.
     * Busca el almacén mediante su identificador y comprueba
     * que exista antes de mostrar el formulario.
     *
     * @param id identificador del almacén que se quiere editar
     * @param model modelo utilizado para enviar los datos a la vista
     * @param redirectAttributes atributos utilizados para enviar mensajes
     *                           durante las redirecciones
     * @param locale configuración regional para obtener los mensajes
     * @return nombre de la vista del formulario o redirección a la lista
     *         de almacenes si se produce algún error
     */
    @GetMapping("/edit")
    public String showEditForm(@RequestParam("id") Long id, Model model,
                               RedirectAttributes redirectAttributes,
                               Locale locale) {
        logger.info("Mostrando formulario de edición para el almacén con ID {}", id);

        try {
            Optional<Almacen> almacen = almacenRepository.findById(id);

            if (almacen.isEmpty()) {
                logger.warn("No se encontró el almacén con ID {}", id);

                redirectAttributes.addFlashAttribute("errorMessage",
                        messageSource.getMessage(
                                "msg.almacen-controller.edit.notFound", null, locale));

                return "redirect:/almacenes";
            }

            model.addAttribute("almacen", almacen.get());

        } catch (DataAccessException e) {
            logger.error("Error al obtener el almacén con ID {}: {}", id, e.getMessage());

            redirectAttributes.addFlashAttribute("errorMessage",
                    messageSource.getMessage(
                            "msg.almacen-controller.edit.error", null, locale));

            return "redirect:/almacenes";
        }

        return "almacenes/almacen-form";
    }

    /**
     * Recibe los datos del formulario para insertar un nuevo almacén.
     * Comprueba que los datos sean válidos antes de guardar el almacén
     * en la base de datos.
     *
     * @param almacen objeto con los datos del almacén recibidos del formulario
     * @param result resultado de las validaciones del formulario
     * @param redirectAttributes atributos utilizados para enviar mensajes
     *                           durante las redirecciones
     * @param locale configuración regional para obtener los mensajes
     * @return nombre de la vista del formulario si hay errores o redirección
     *         a la lista de almacenes
     */
    @PostMapping("/insert")
    public String insertAlmacen(@Valid @ModelAttribute("almacen") Almacen almacen,
                                BindingResult result,
                                RedirectAttributes redirectAttributes,
                                Locale locale) {
        logger.info("Insertando nuevo almacén en la ciudad {}", almacen.getCiudad());

        try {
            if (result.hasErrors()) {
                return "almacenes/almacen-form";
            }

            almacenRepository.save(almacen);

            logger.info("Almacén insertado con éxito.");

        } catch (DataAccessException e) {
            logger.error("Error al insertar el almacén: {}", e.getMessage());

            redirectAttributes.addFlashAttribute("errorMessage",
                    messageSource.getMessage(
                            "msg.almacen-controller.insert.error", null, locale));
        }

        return "redirect:/almacenes";
    }

    /**
     * Recibe los datos del formulario para actualizar un almacén existente.
     * Comprueba que los datos sean válidos antes de guardar los cambios
     * en la base de datos.
     *
     * @param almacen objeto con los datos actualizados del almacén
     * @param result resultado de las validaciones del formulario
     * @param redirectAttributes atributos utilizados para enviar mensajes
     *                           durante las redirecciones
     * @param locale configuración regional para obtener los mensajes
     * @return nombre de la vista del formulario si hay errores o redirección
     *         a la lista de almacenes
     */
    @PostMapping("/update")
    public String updateAlmacen(@Valid @ModelAttribute("almacen") Almacen almacen,
                                BindingResult result,
                                RedirectAttributes redirectAttributes,
                                Locale locale) {
        logger.info("Actualizando almacén con ID {}", almacen.getId());

        try {
            if (result.hasErrors()) {
                return "almacenes/almacen-form";
            }

            almacenRepository.save(almacen);

            logger.info("Almacén con ID {} actualizado con éxito.", almacen.getId());

        } catch (DataAccessException e) {
            logger.error("Error al actualizar el almacén con ID {}: {}",
                    almacen.getId(), e.getMessage());

            redirectAttributes.addFlashAttribute("errorMessage",
                    messageSource.getMessage(
                            "msg.almacen-controller.update.error", null, locale));
        }

        return "redirect:/almacenes";
    }

    /**
     * Elimina un almacén mediante su identificador.
     * Si el almacén tiene una licencia o pasillos asociados, no se puede
     * eliminar y se muestra el mensaje correspondiente.
     *
     * @param id identificador del almacén que se quiere eliminar
     * @param redirectAttributes atributos utilizados para enviar mensajes
     *                           durante las redirecciones
     * @param locale configuración regional para obtener los mensajes
     * @return redirección a la lista de almacenes
     */
    @PostMapping("/delete")
    public String deleteAlmacen(@RequestParam("id") Long id,
                                RedirectAttributes redirectAttributes,
                                Locale locale) {
        logger.info("Eliminando almacén con ID {}", id);

        try {
            // Un almacén con licencia asociada no se puede eliminar.
            if (licenciaAlmacenRepository.existsByAlmacenId(id)) {
                logger.warn("No se puede eliminar el almacén con ID {}: tiene licencia.", id);

                redirectAttributes.addFlashAttribute("errorMessage",
                        messageSource.getMessage(
                                "msg.almacen-controller.delete.hasDependencies", null, locale));

                return "redirect:/almacenes";
            }

            almacenRepository.deleteById(id);

            logger.info("Almacén con ID {} eliminado con éxito.", id);

        } catch (DataIntegrityViolationException e) {
            // La clave ajena de pasillos o licencias_almacen impide borrar el almacén
            logger.warn("No se puede eliminar el almacén con ID {}: tiene pasillos o licencia.", id);

            redirectAttributes.addFlashAttribute("errorMessage",
                    messageSource.getMessage(
                            "msg.almacen-controller.delete.hasDependencies", null, locale));

        } catch (DataAccessException e) {
            logger.error("Error al eliminar el almacén con ID {}: {}", id, e.getMessage());

            redirectAttributes.addFlashAttribute("errorMessage",
                    messageSource.getMessage(
                            "msg.almacen-controller.delete.error", null, locale));
        }

        return "redirect:/almacenes";
    }
}
