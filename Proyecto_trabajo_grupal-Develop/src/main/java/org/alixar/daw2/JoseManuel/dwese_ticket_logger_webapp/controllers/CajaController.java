package org.alixar.daw2.JoseManuel.dwese_ticket_logger_webapp.controllers;

import jakarta.validation.Valid;
import org.alixar.daw2.JoseManuel.dwese_ticket_logger_webapp.entities.Caja;
import org.alixar.daw2.JoseManuel.dwese_ticket_logger_webapp.entities.Estanteria;
import org.alixar.daw2.JoseManuel.dwese_ticket_logger_webapp.repositories.CajaRepository;
import org.alixar.daw2.JoseManuel.dwese_ticket_logger_webapp.repositories.EstanteriaRepository;
import org.alixar.daw2.JoseManuel.dwese_ticket_logger_webapp.repositories.EtiquetaProductoRepository;
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
 * con las cajas del almacén.
 * Permite listar, crear, editar y eliminar cajas, además de
 * gestionar las validaciones y los posibles errores.
 */
@Controller
@RequestMapping("/cajas")
public class CajaController {

    private static final Logger logger =
            LoggerFactory.getLogger(CajaController.class);

    @Autowired
    private CajaRepository cajaRepository;

    @Autowired
    private EstanteriaRepository estanteriaRepository;

    @Autowired
    private EtiquetaProductoRepository etiquetaProductoRepository;

    @Autowired
    private MessageSource messageSource;

    /**
     * Muestra la lista de todas las cajas registradas.
     * Obtiene los datos de la base de datos y los envía a la vista.
     * Si se produce un error, muestra un mensaje de error.
     *
     * @param model modelo utilizado para enviar los datos a la vista
     * @param locale configuración regional para obtener los mensajes
     * @return nombre de la vista que muestra la lista de cajas
     */
    @GetMapping
    public String listCajas(Model model, Locale locale) {
        logger.info("Solicitando la lista de cajas...");

        List<Caja> listCajas = null;

        try {
            listCajas = cajaRepository.findAll();
            logger.info("Se han cargado {} cajas.", listCajas.size());
        } catch (DataAccessException e) {
            logger.error("Error al listar las cajas: {}", e.getMessage());

            model.addAttribute("errorMessage",
                    messageSource.getMessage(
                            "msg.caja-controller.list.error", null, locale));
        }

        model.addAttribute("listCajas", listCajas);

        return "cajas/caja";
    }

    /**
     * Muestra el formulario para crear una nueva caja.
     * Inicializa un objeto Caja y una estantería vacía para
     * poder seleccionar una en el formulario.
     *
     * @param model modelo utilizado para enviar los datos a la vista
     * @param redirectAttributes atributos utilizados para enviar mensajes
     *                           durante las redirecciones
     * @param locale configuración regional para obtener los mensajes
     * @return nombre de la vista del formulario de cajas
     */
    @GetMapping("/new")
    public String showNewForm(Model model, RedirectAttributes redirectAttributes,
                              Locale locale) {
        logger.info("Mostrando formulario para nueva caja.");

        Caja caja = new Caja();
        // Estantería vacía para que el formulario pueda enlazar estanteria.id.
        caja.setEstanteria(new Estanteria());

        model.addAttribute("caja", caja);

        return loadEstanteriasAndShowForm(model, redirectAttributes, locale);
    }

    /**
     * Muestra el formulario para editar una caja existente.
     * Busca la caja mediante su identificador y comprueba
     * que exista antes de mostrar el formulario.
     *
     * @param id identificador de la caja que se quiere editar
     * @param model modelo utilizado para enviar los datos a la vista
     * @param redirectAttributes atributos utilizados para enviar mensajes
     *                           durante las redirecciones
     * @param locale configuración regional para obtener los mensajes
     * @return nombre de la vista del formulario o redirección a la lista
     *         de cajas si se produce algún error
     */
    @GetMapping("/edit")
    public String showEditForm(@RequestParam("id") Long id, Model model,
                               RedirectAttributes redirectAttributes,
                               Locale locale) {
        logger.info("Mostrando formulario de edición para la caja con ID {}", id);

        try {
            Optional<Caja> caja = cajaRepository.findById(id);

            if (caja.isEmpty()) {
                logger.warn("No se encontró la caja con ID {}", id);

                redirectAttributes.addFlashAttribute("errorMessage",
                        messageSource.getMessage(
                                "msg.caja-controller.edit.notFound", null, locale));

                return "redirect:/cajas";
            }

            model.addAttribute("caja", caja.get());

        } catch (DataAccessException e) {
            logger.error("Error al obtener la caja con ID {}: {}", id, e.getMessage());

            redirectAttributes.addFlashAttribute("errorMessage",
                    messageSource.getMessage(
                            "msg.caja-controller.edit.error", null, locale));

            return "redirect:/cajas";
        }

        return loadEstanteriasAndShowForm(model, redirectAttributes, locale);
    }

    /**
     * Recibe los datos del formulario para insertar una nueva caja.
     * Comprueba que los datos sean válidos y que se haya seleccionado
     * una estantería antes de guardar la caja en la base de datos.
     *
     * @param caja objeto con los datos de la caja recibidos del formulario
     * @param result resultado de las validaciones del formulario
     * @param model modelo utilizado para enviar los datos a la vista
     * @param redirectAttributes atributos utilizados para enviar mensajes
     *                           durante las redirecciones
     * @param locale configuración regional para obtener los mensajes
     * @return nombre de la vista del formulario si hay errores o redirección
     *         a la lista de cajas
     */
    @PostMapping("/insert")
    public String insertCaja(@Valid @ModelAttribute("caja") Caja caja,
                             BindingResult result, Model model,
                             RedirectAttributes redirectAttributes,
                             Locale locale) {
        logger.info("Insertando nueva caja con contenido {}", caja.getContenido());

        try {
            checkEstanteriaSelected(caja, result);

            if (result.hasErrors()) {
                return loadEstanteriasAndShowForm(model, redirectAttributes, locale);
            }

            cajaRepository.save(caja);

            logger.info("Caja insertada con éxito.");

        } catch (DataAccessException e) {
            logger.error("Error al insertar la caja: {}", e.getMessage());

            model.addAttribute("errorMessage",
                    messageSource.getMessage(
                            "msg.caja-controller.insert.error", null, locale));
            try {
                model.addAttribute("listEstanterias", estanteriaRepository.findAll());
                return "cajas/caja-form";
            } catch (DataAccessException loadException) {
                logger.error("Error al recargar las estanterías: {}", loadException.getMessage());
                redirectAttributes.addFlashAttribute("errorMessage",
                        messageSource.getMessage(
                                "msg.caja-controller.estanterias.error", null, locale));
                return "redirect:/cajas";
            }
        }

        return "redirect:/cajas";
    }

    /**
     * Recibe los datos del formulario para actualizar una caja existente.
     * Comprueba que los datos sean válidos y que se haya seleccionado
     * una estantería antes de guardar los cambios en la base de datos.
     *
     * @param caja objeto con los datos actualizados de la caja
     * @param result resultado de las validaciones del formulario
     * @param model modelo utilizado para enviar los datos a la vista
     * @param redirectAttributes atributos utilizados para enviar mensajes
     *                           durante las redirecciones
     * @param locale configuración regional para obtener los mensajes
     * @return nombre de la vista del formulario si hay errores o redirección
     *         a la lista de cajas
     */
    @PostMapping("/update")
    public String updateCaja(@Valid @ModelAttribute("caja") Caja caja,
                             BindingResult result, Model model,
                             RedirectAttributes redirectAttributes,
                             Locale locale) {
        logger.info("Actualizando caja con ID {}", caja.getId());

        try {
            checkEstanteriaSelected(caja, result);

            if (result.hasErrors()) {
                return loadEstanteriasAndShowForm(model, redirectAttributes, locale);
            }

            cajaRepository.save(caja);

            logger.info("Caja con ID {} actualizada con éxito.", caja.getId());

        } catch (DataAccessException e) {
            logger.error("Error al actualizar la caja con ID {}: {}",
                    caja.getId(), e.getMessage());

            model.addAttribute("errorMessage",
                    messageSource.getMessage(
                            "msg.caja-controller.update.error", null, locale));
            try {
                model.addAttribute("listEstanterias", estanteriaRepository.findAll());
                return "cajas/caja-form";
            } catch (DataAccessException loadException) {
                logger.error("Error al recargar las estanterías: {}", loadException.getMessage());
                redirectAttributes.addFlashAttribute("errorMessage",
                        messageSource.getMessage(
                                "msg.caja-controller.estanterias.error", null, locale));
                return "redirect:/cajas";
            }
        }

        return "redirect:/cajas";
    }

    /**
     * Elimina una caja mediante su identificador.
     * Si la caja tiene una etiqueta asociada no se puede eliminar
     * y se muestra el mensaje correspondiente.
     *
     * @param id identificador de la caja que se quiere eliminar
     * @param redirectAttributes atributos utilizados para enviar mensajes
     *                           durante las redirecciones
     * @param locale configuración regional para obtener los mensajes
     * @return redirección a la lista de cajas
     */
    @PostMapping("/delete")
    public String deleteCaja(@RequestParam("id") Long id,
                             RedirectAttributes redirectAttributes,
                             Locale locale) {
        logger.info("Eliminando caja con ID {}", id);

        try {
            // Una caja con etiqueta asociada no se puede eliminar.
            if (etiquetaProductoRepository.existsByCajaId(id)) {
                logger.warn("No se puede eliminar la caja con ID {}: tiene etiqueta.", id);

                redirectAttributes.addFlashAttribute("errorMessage",
                        messageSource.getMessage(
                                "msg.caja-controller.delete.hasEtiqueta", null, locale));

                return "redirect:/cajas";
            }

            cajaRepository.deleteById(id);

            logger.info("Caja con ID {} eliminada con éxito.", id);

        } catch (DataIntegrityViolationException e) {
            // La clave ajena de etiquetas_producto impide borrar una caja con etiqueta
            logger.warn("No se puede eliminar la caja con ID {}: tiene etiqueta.", id);

            redirectAttributes.addFlashAttribute("errorMessage",
                    messageSource.getMessage(
                            "msg.caja-controller.delete.hasEtiqueta", null, locale));

        } catch (DataAccessException e) {
            logger.error("Error al eliminar la caja con ID {}: {}", id, e.getMessage());

            redirectAttributes.addFlashAttribute("errorMessage",
                    messageSource.getMessage(
                            "msg.caja-controller.delete.error", null, locale));
        }

        return "redirect:/cajas";
    }

    /**
     * Comprueba que se haya seleccionado una estantería en el desplegable.
     * Si no se ha seleccionado ninguna, añade un error de validación
     * al campo correspondiente.
     *
     * @param caja caja cuyos datos se quieren comprobar
     * @param result resultado donde se registra el error de validación
     */
    private void checkEstanteriaSelected(Caja caja, BindingResult result) {
        if (caja.getEstanteria() == null || caja.getEstanteria().getId() == null) {
            result.rejectValue("estanteria.id", "msg.caja.estanteria.notNull");
        }
    }

    /**
     * Carga la lista de estanterías y la añade al modelo para mostrar
     * el formulario de creación o edición de una caja.
     * Si se produce un error al obtener las estanterías, realiza
     * una redirección a la lista de cajas mostrando un mensaje.
     *
     * @param model modelo utilizado para enviar las estanterías a la vista
     * @param redirectAttributes atributos utilizados para enviar mensajes
     *                           durante las redirecciones
     * @param locale configuración regional para obtener los mensajes
     * @return nombre de la vista del formulario o redirección a la lista
     *         de cajas si se produce un error
     */
    private String loadEstanteriasAndShowForm(Model model,
                                              RedirectAttributes redirectAttributes,
                                              Locale locale) {
        try {
            model.addAttribute("listEstanterias", estanteriaRepository.findAll());

            return "cajas/caja-form";

        } catch (DataAccessException e) {
            logger.error("Error al cargar las estanterías: {}", e.getMessage());

            redirectAttributes.addFlashAttribute("errorMessage",
                    messageSource.getMessage(
                            "msg.caja-controller.estanterias.error", null, locale));

            return "redirect:/cajas";
        }
    }
}
