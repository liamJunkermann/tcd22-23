csu33031_protocol = Proto("csu33031", "Flow Forwarding Protocol")
packet_type = ProtoField.uint8("csu33031.packet_type", "Packet Type", base.DEC)
packet_name = ProtoField.string("csu33031.packet_name", "Packet Name")
dst_length = ProtoField.uint8("csu33031.dst_length", "Destination Length", base.DEC)
destination = ProtoField.string("csu33031.dest", "Destination")
message = ProtoField.string("csu33031.msg", "Message")

csu33031_protocol.fields = { packet_type, packet_name, dst_length, destination, message }

function get_packet_type(type)
    local type_name = "unknown"
    if type == 0 then type_name = "HELLO"
    elseif type == 1 then type_name = "PACKET_IN"
    elseif type == 2 then type_name = "FWD_MOD"
    elseif type == 3 then type_name = "NETWORK_ID"
    end
    return type_name
end

function csu33031_protocol.dissector(buffer, pinfo, tree)
    local length = buffer:len()
    if length == 0 then return end
    pinfo.cols.protocol = csu33031_protocol.name
    local subtree = tree:add(csu33031_protocol, buffer(), "LJ PROTOCOL DATA")
    local type = buffer(0, 1):le_uint()
    local type_name = get_packet_type(type)

    subtree:add_le(packet_type, buffer(0, 1)) -- :append_text(" ("..type_name..")")
    subtree:add_le(packet_name, type_name)

    if (type_name ~= "HELLO") then
        local destinationLn = buffer(1, 1):le_uint()
        subtree:add_le(dst_length, buffer(1, 1))
        local dest = buffer(2, destinationLn):string()
        subtree:add_le(destination, dest)
        subtree:add_le(message, buffer(2 + destinationLn, length - destinationLn - 2):string())
        return
    end
end

local udp_port = DissectorTable.get('udp.port')
udp_port:add(54321, csu33031_protocol)
