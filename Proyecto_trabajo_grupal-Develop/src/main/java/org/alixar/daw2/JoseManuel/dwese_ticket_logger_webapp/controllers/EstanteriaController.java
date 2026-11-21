package org.alixar.daw2.JoseManuel.dwese_ticket_logger_webapp.controllers;

import jakarta.validation.Valid;
import org.alixar.daw2.JoseManuel.dwese_ticket_logger_webapp.entities.Estanteria;
import org.alixar.daw2.JoseManuel.dwese_ticket_logger_webapp.entities.Pasillo;
import org.alixar.daw2.JoseManuel.dwese_ticket_logger_webapp.repositories.EstanteriaRepository;
import org.alixar.daw2.JoseManuel.dwese_ticket_logger_webapp.repositories.PasilloRepository;
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
 * con las estanterías del almacén.
 * Permite listar, crear, editar y eliminar estanterías, además de
 * gestionar las validaciones y los posibles errores.
 */
@Controller
@RequestMapping("/estanterias")
public class EstanteriaController {


    private static final Logger logger =
            LoggerFactory.getLogger(EstanteriaController.class);

    @Autowired
    private EstanteriaRepository estanteriaRepository;

    @Autowired
    private PasilloRepository pasilloRepository;

    @Autowired
    private MessageSource messageSource;

    /**
     * Muestra la lista de todas las estanterías registradas.
     * Obtiene los datos de la base de datos y los envía a la vista.
     * Si se produce un error, muestra un mensaje de error.
     *
     * @param model modelo utilizado para enviar los datos a la vista
     * @param locale configuración regional para obtener los mensajes
     * @return nombre de la vista que muestra la lista de estanterías
     */
    @GetMapping
    public String listEstanterias(Model model, Locale locale) {
        logger.info("Solicitando la lista de estanterías...");

        List<Estanteria> listEstanterias = null;

        try {
            listEstanterias = estanteriaRepository.findAll();
            logger.info("Se han cargado {} estanterías.", listEstanterias.size());
        } catch (DataAccessException e) {
            logger.error("Error al listar las estanterías: {}", e.getMessage());

            model.addAttribute("errorMessage",
                    messageSource.getMessage(
                            "msg.estanteria-controller.list.error", null, locale));
        }

        model.addAttribute("listEstanterias", listEstanterias);

        return "estanterias/estanteria";
    }

    /**
     * Muestra el formulario para crear una nueva estantería.
     * Inicializa un objeto Estanteria y un pasillo vacío para
     * poder seleccionar uno en el formulario.
     *
     * @param model modelo utilizado para enviar los datos a la vista
     * @param redirectAttributes atributos utilizados para enviar mensajes
     *                           durante las redirecciones
     * @param locale configuración regional para obtener los mensajes
     * @return nombre de la vista del formulario de estanterías
     */
    @GetMapping("/new")
    public String showNewForm(Model model, RedirectAttributes redirectAttributes,
                              Locale locale) {
        logger.info("Mostrando formulario para nueva estantería.");

        Estanteria estanteria = new Estanteria();
        // Pasillo vacío para que el formulario pueda enlazar pasillo.id.
        estanteria.setPasillo(new Pasillo());

        model.addAttribute("estanteria", estanteria);

        return loadPasillosAndShowForm(model, redirectAttributes, locale);
    }

    /**
     * Muestra el formulario para editar una estantería existente.
     * Busca la estantería mediante su identificador y comprueba
     * que exista antes de mostrar el formulario.
     *
     * @param id identificador de la estantería que se quiere editar
     * @param model modelo utilizado para enviar los datos a la vista
     * @param redirectAttributes atributos utilizados para enviar mensajes
     *                           durante las redirecciones
     * @param locale configuración regional para obtener los mensajes
     * @return nombre de la vista del formulario o redirección a la lista
     *         de estanterías si se produce algún error
     */
    @GetMapping("/edit")
    public String showEditForm(@RequestParam("id") Long id, Model model,
                               RedirectAttributes redirectAttributes,
                               Locale locale) {
        logger.info("Mostrando formulario de edición para la estantería con ID {}", id);

        try {
            Optional<Estanteria> estanteria = estanteriaRepository.findById(id);

            if (estanteria.isEmpty()) {
                logger.warn("No se encontró la estantería con ID {}", id);

                redirectAttributes.addFlashAttribute("errorMessage",
                        messageSource.getMessage(
                                "msg.estanteria-controller.edit.notFound", null, locale));

                return "redirect:/estanterias";
            }

            model.addAttribute("estanteria", estanteria.get());

        } catch (DataAccessException e) {
            logger.error("Error al obtener la estantería con ID {}: {}", id, e.getMessage());

            redirectAttributes.addFlashAttribute("errorMessage",
                    messageSource.getMessage(
                            "msg.estanteria-controller.edit.error", null, locale));

            return "redirect:/estanterias";
        }

        return loadPasillosAndShowForm(model, redirectAttributes, locale);
    }

    /**
     * Recibe los datos del formulario para insertar una nueva estantería.
     * Comprueba que los datos sean válidos y que se haya seleccionado
     * un pasillo antes de guardar la estantería en la base de datos.
     *
     * @param estanteria objeto con los datos de la estantería recibidos del formulario
     * @param result resultado de las validaciones del formulario
     * @param model modelo utilizado para enviar los datos a la vista
     * @param redirectAttributes atributos utilizados para enviar mensajes
     *                           durante las redirecciones
     * @param locale configuración regional para obtener los mensajes
     * @return nombre de la vista del formulario si hay errores o redirección
     *         a la lista de estanterías
     */
    @PostMapping("/insert")
    public String insertEstanteria(@Valid @ModelAttribute("estanteria") Estanteria estanteria,
                                   BindingResult result, Model model,
                                   RedirectAttributes redirectAttributes,
                                   Locale locale) {
        logger.info("Insertando nueva estantería de material {}", estanteria.getMaterial());

        try {
            checkPasilloSelected(estanteria, result);

            if (result.hasErrors()) {
                return loadPasillosAndShowForm(model, redirectAttributes, locale);
            }

            estanteriaRepository.save(estanteria);

            logger.info("Estantería insertada con éxito.");

        } catch (DataAccessException e) {
            logger.error("Error al insertar la estantería: {}", e.getMessage());

            redirectAttributes.addFlashAttribute("errorMessage",
                    messageSource.getMessage(
                            "msg.estanteria-controller.insert.error", null, locale));
        }

        return "redirect:/estanterias";
    }

    /**
     * Recibe los datos del formulario para actualizar una estantería existente.
     * Comprueba que los datos sean válidos y que se haya seleccionado
     * un pasillo antes de guardar los cambios en la base de datos.
     *
     * @param estanteria objeto con los datos actualizados de la estantería
     * @param result resultado de las validaciones del formulario
     * @param model modelo utilizado para enviar los datos a la vista
     * @param redirectAttributes atributos utilizados para enviar mensajes
     *                           durante las redirecciones
     * @param locale configuración regional para obtener los mensajes
     * @return nombre de la vista del formulario si hay errores o redirección
     *         a la lista de estanterías
     */
    @PostMapping("/update")
    public String updateEstanteria(@Valid @ModelAttribute("estanteria") Estanteria estanteria,
                                   BindingResult result, Model model,
                                   RedirectAttributes redirectAttributes,
                                   Locale locale) {
        logger.info("Actualizando estantería con ID {}", estanteria.getId());

        try {
            checkPasilloSelected(estanteria, result);

            if (result.hasErrors()) {
                return loadPasillosAndShowForm(model, redirectAttributes, locale);
            }

            estanteriaRepository.save(estanteria);

            logger.info("Estantería con ID {} actualizada con éxito.", estanteria.getId());

        } catch (DataAccessException e) {
            logger.error("Error al actualizar la estantería con ID {}: {}",
                    estanteria.getId(), e.getMessage());

            redirectAttributes.addFlashAttribute("errorMessage",
                    messageSource.getMessage(
                            "msg.estanteria-controller.update.error", null, locale));
        }

        return "redirect:/estanterias";
    }

    /**
     * Elimina una estantería mediante su identificador.
     * Si la estantería tiene cajas asociadas, la base de datos
     * impide su eliminación y se muestra el mensaje correspondiente.
     *
     * @param id identificador de la estantería que se quiere eliminar
     * @param redirectAttributes atributos utilizados para enviar mensajes
     *                           durante las redirecciones
     * @param locale configuración regional para obtener los mensajes
     * @return redirección a la lista de estanterías
     */
    @PostMapping("/delete")
    public String deleteEstanteria(@RequestParam("id") Long id,
                                   RedirectAttributes redirectAttributes,
                                   Locale locale) {
        logger.info("Eliminando estantería con ID {}", id);

        try {
            estanteriaRepository.deleteById(id);

            logger.info("Estantería con ID {} eliminada con éxito.", id);

        } catch (DataIntegrityViolationException e) {
            // La clave ajena de cajas impide borrar una estantería con cajas
            logger.warn("No se puede eliminar la estantería con ID {}: tiene cajas.", id);

            redirectAttributes.addFlashAttribute("errorMessage",
                    messageSource.getMessage(
                            "msg.estanteria-controller.delete.hasCajas", null, locale));

        } catch (DataAccessException e) {
            logger.error("Error al eliminar la estantería con ID {}: {}", id, e.getMessage());

            redirectAttributes.addFlashAttribute("errorMessage",
                    messageSource.getMessage(
                            "msg.estanteria-controller.delete.error", null, locale));
        }

        return "redirect:/estanterias";
    }

    /**
     * Comprueba que se haya seleccionado un pasillo en el desplegable.
     * Si no se ha seleccionado ninguno, añade un error de validación
     * al campo correspondiente.
     *
     * @param estanteria estantería cuyos datos se quieren comprobar
     * @param result resultado donde se registra el error de validación
     */
    private void checkPasilloSelected(Estanteria estanteria, BindingResult result) {
        if (estanteria.getPasillo() == null || estanteria.getPasillo().getId() == null) {
            result.rejectValue("pasillo.id", "msg.estanteria.pasillo.notNull");
        }
    }

    /**
     * Carga la lista de pasillos y la añade al modelo para mostrar
     * el formulario de creación o edición de una estantería.
     * Si se produce un error al obtener los pasillos, realiza
     * una redirección a la lista de estanterías mostrando un mensaje.
     *
     * @param model modelo utilizado para enviar los pasillos a la vista
     * @param redirectAttributes atributos utilizados para enviar mensajes
     *                           durante las redirecciones
     * @param locale configuración regional para obtener los mensajes
     * @return nombre de la vista del formulario o redirección a la lista
     *         de estanterías si se produce un error
     */
    private String loadPasillosAndShowForm(Model model,
                                           RedirectAttributes redirectAttributes,
                                           Locale locale) {
        try {
            model.addAttribute("listPasillos", pasilloRepository.findAll());

            return "estanterias/estanteria-form";

        } catch (DataAccessException e) {
            logger.error("Error al cargar los pasillos: {}", e.getMessage());

            redirectAttributes.addFlashAttribute("errorMessage",
                    messageSource.getMessage(
                            "msg.estanteria-controller.pasillos.error", null, locale));

            return "redirect:/estanterias";
        }
    }


}